package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.BrowseViewConfiguration;
import com.zutubi.util.Sort;
import com.zutubi.util.bean.ObjectFactory;

import java.util.*;

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

    public ResultState getStateInProgress()
    {
        return ResultState.IN_PROGRESS;
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

        Collections.sort(invalidProjects, new ProjectComparator(new Sort.StringComparator()));

        ProjectsModelsHelper helper = objectFactory.buildBean(ProjectsModelsHelper.class);
        models = helper.createProjectsModels(browseConfig);
        return SUCCESS;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
