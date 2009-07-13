package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.LabelProjectTuple;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectGroup;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.BrowseViewConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.TruePredicate;
import com.zutubi.util.bean.ObjectFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Action to load the projects data for the browse view.
 */
public class BrowseDataAction extends ProjectActionSupport
{
    private BrowseModel model = new BrowseModel();

    private ObjectFactory objectFactory;
    private ConfigurationManager configurationManager;

    public BrowseModel getModel()
    {
        return model;
    }

    public String execute() throws Exception
    {
        User user = getLoggedInUser();
        final BrowseViewConfiguration browseConfig = user == null ? new BrowseViewConfiguration() : user.getPreferences().getBrowseView();
        Set<LabelProjectTuple> collapsed = user == null ? Collections.<LabelProjectTuple>emptySet() : user.getBrowseViewCollapsed();

        List<Project> projects = projectManager.getProjects(true);

        // Filter invalid projects into a separate list.
        List<String> invalidProjects = new LinkedList<String>();
        for (Project project: projects)
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
        model.setProjectGroups(helper.createProjectsModels(user, browseConfig, collapsed, urls, new TruePredicate<Project>(), new Predicate<ProjectGroup>()
        {
            public boolean satisfied(ProjectGroup projectGroup)
            {
                return browseConfig.isGroupsShown();
            }
        }, true));
        
        return SUCCESS;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
