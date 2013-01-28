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

import java.util.*;

/**
 * Action to load the projects data for the browse view.
 */
public class BrowseDataAction extends ProjectActionSupport
{
    private BrowseModel model = new BrowseModel();

    private ConfigurationManager configurationManager;
    private TransactionManager pulseTransactionManager;

    public BrowseModel getModel()
    {
        return model;
    }

    public String execute() throws Exception
    {
        return pulseTransactionManager.runInTransaction(new NullaryFunction<String>()
        {
            public String process()
            {
                User user = getLoggedInUser();
                model.setProjectsFilter(user == null ? "" : user.getBrowseViewFilter());
                
                final BrowseViewConfiguration browseConfig = user == null ? new BrowseViewConfiguration() : user.getPreferences().getBrowseView();
                Set<LabelProjectTuple> collapsed = user == null ? Collections.<LabelProjectTuple>emptySet() : user.getBrowseViewCollapsed();

                Collection<ProjectConfiguration> allProjects = projectManager.getAllProjectConfigs(true);

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
}
