package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.xwork.actions.project.ProjectsModel;
import flexjson.JSON;

import java.util.List;

/**
 * Holder for all JSON data sent to the UI to render the dashboard.
 */
public class DashboardModel
{
    private List<String> contactPointsWithErrors;
    private List<ProjectsModel> projects;
    private List<ChangelistModel> myChanges;
    private List<ChangelistModel> myProjectChanges;

    public DashboardModel(List<String> contactPointsWithErrors, List<ProjectsModel> projects, List<ChangelistModel> myChanges, List<ChangelistModel> myProjectChanges)
    {
        this.contactPointsWithErrors = contactPointsWithErrors;
        this.projects = projects;
        this.myChanges = myChanges;
        this.myProjectChanges = myProjectChanges;
    }

    @JSON
    public List<String> getContactPointsWithErrors()
    {
        return contactPointsWithErrors;
    }

    @JSON
    public List<ProjectsModel> getProjects()
    {
        return projects;
    }

    @JSON
    public List<ChangelistModel> getMyChanges()
    {
        return myChanges;
    }

    @JSON
    public List<ChangelistModel> getMyProjectChanges()
    {
        return myProjectChanges;
    }
}
