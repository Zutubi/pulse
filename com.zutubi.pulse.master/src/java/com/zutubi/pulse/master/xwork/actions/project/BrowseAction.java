package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.NamedEntityComparator;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectGroup;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.BrowseViewConfiguration;
import com.zutubi.util.Predicate;
import com.zutubi.util.TruePredicate;
import com.zutubi.util.bean.ObjectFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Action for the main browse view which displays the status of all projects.
 */
public class BrowseAction extends ProjectActionSupport
{
    private List<ProjectsModel> models = new LinkedList<ProjectsModel>();
    private List<Project> invalidProjects = new LinkedList<Project>();
    private BrowseViewConfiguration browseConfig = new BrowseViewConfiguration();

    private ObjectFactory objectFactory;

    public List<ProjectsModel> getModels()
    {
        return models;
    }

    public List<Project> getInvalidProjects()
    {
        return invalidProjects;
    }

    public List<String> getColumns()
    {
        return browseConfig.getColumns();
    }

    public String execute() throws Exception
    {
        User user = getLoggedInUser();
        if(user != null)
        {
            browseConfig = user.getPreferences().getBrowseView();
        }

        List<Project> projects = projectManager.getProjects(true);

        // Filter invalid projects into a separate list.
        for (Project project: projects)
        {
            if (!projectManager.isProjectValid(project))
            {
                invalidProjects.add(project);
            }
        }

        Collections.sort(invalidProjects, new NamedEntityComparator());

        ProjectsModelsHelper helper = objectFactory.buildBean(ProjectsModelsHelper.class);
        models = helper.createProjectsModels(browseConfig, new TruePredicate<Project>(), new Predicate<ProjectGroup>()
        {
            public boolean satisfied(ProjectGroup projectGroup)
            {
                return browseConfig.isGroupsShown();
            }
        });
        
        return SUCCESS;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
