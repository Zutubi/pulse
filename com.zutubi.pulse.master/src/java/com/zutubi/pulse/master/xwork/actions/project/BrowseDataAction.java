/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.user.BrowseViewConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.Sort;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Action to load the projects data for the browse view.
 */
public class BrowseDataAction extends ProjectActionSupport
{
    public static final String PROPERTY_DISABLE_BROWSE_CONSISTENCY = "pulse.disable.browse.consistency";

    private BrowseModel model = new BrowseModel();

    private ConfigurationManager configurationManager;
    private TransactionManager pulseTransactionManager;
    private HibernateTransactionManager transactionManager;

    public BrowseModel getModel()
    {
        return model;
    }

    public String execute() throws Exception
    {
        // This Tove transaction gives a consistent view of configuration (nice, potentially not necessary) and avoids
        // multiple copies of transactional resources (e.g. copy of instance cache structure) which were a performance
        // issue in Pulse 2.1 days (light testing on 2.6.x suggests this is no longer an issue, and using this outer
        // transaction may be slower).  The big downside is serialisation: tove transactions currently take a mutex.
        // Hence we're experimenting with turning it off via a system property.
        //
        // An inner Hibernate transaction is used (in assembleModel) so we don't need to create/commit a bunch of
        // transactions for all the contained manager/dao calls.  There is one commit, and everything should be marked
        // read only (to avoid costly dirty-checking in Hibernate).
        if (Boolean.getBoolean(PROPERTY_DISABLE_BROWSE_CONSISTENCY))
        {
            return assembleModel();
        }
        else
        {
            return pulseTransactionManager.runInTransaction(new NullaryFunction<String>()
            {
                public String process()
                {
                    return assembleModel();
                }
            });
        }
    }

    private String assembleModel()
    {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(true);
        return template.execute(new TransactionCallback<String>()
        {
            public String doInTransaction(TransactionStatus status)
            {
                User user = getLoggedInUser();
                model.setProjectsFilter(user == null ? "" : user.getBrowseViewFilter());

                final BrowseViewConfiguration browseConfig = user == null ? new BrowseViewConfiguration() : user.getPreferences().getBrowseView();
                Set<LabelProjectTuple> collapsed = user == null ? Collections.<LabelProjectTuple>emptySet() : user.getBrowseViewCollapsed();

                Iterable<ProjectConfiguration> allProjects = projectManager.getAllProjectConfigs(true);

                // Filter invalid projects into a separate list.
                List<String> invalidProjects = new LinkedList<String>();
                for (ProjectConfiguration project: Iterables.filter(allProjects, ProjectPredicates.concrete()))
                {
                    if (!projectManager.isProjectValid(project))
                    {
                        invalidProjects.add(project.getName());
                    }
                }

                Collections.sort(invalidProjects, new Sort.StringComparator());
                model.setInvalidProjects(invalidProjects);

                Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
                ProjectsModelsHelper helper = objectFactory.buildBean(ProjectsModelsHelper.class);
                model.setProjectGroups(helper.createProjectsModels(user, browseConfig, collapsed, urls, Predicates.<Project>alwaysTrue(), new Predicate<ProjectGroup>()
                {
                    public boolean apply(ProjectGroup projectGroup)
                    {
                        return browseConfig.isGroupsShown();
                    }
                }, true));

                return SUCCESS;
            }
        });
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setPulseTransactionManager(TransactionManager pulseTransactionManager)
    {
        this.pulseTransactionManager = pulseTransactionManager;
    }

    public void setTransactionManager(HibernateTransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }
}
