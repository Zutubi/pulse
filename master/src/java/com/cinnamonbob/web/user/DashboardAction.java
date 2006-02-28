package com.cinnamonbob.web.user;

import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.model.*;
import com.cinnamonbob.security.AcegiUtils;
import com.cinnamonbob.web.ActionSupport;

import java.util.LinkedList;
import java.util.List;

/**
 * Action to view the user's dashboard: their own Bob "homepage".
 */
public class DashboardAction extends ActionSupport
{
    private User user;
    private List<Project> projects;
    private List<BuildResult> latestBuilds;
    private List<Changelist> changelists;
    private ProjectManager projectManager;
    private BuildManager buildManager;

    public User getUser()
    {
        return user;
    }

    public List<Project> getProjects()
    {
        return projects;
    }

    public List<BuildResult> getLatestBuilds()
    {
        return latestBuilds;
    }

    public List<Changelist> getChangelists()
    {
        return changelists;
    }

    public String execute() throws Exception
    {
        user = AcegiUtils.getLoggedInUser();
        if (user == null)
        {
            return ERROR;
        }

        projects = projectManager.getAllProjects();
        latestBuilds = new LinkedList<BuildResult>();

        for (Project p : projects)
        {
            List<BuildResult> build = buildManager.getLatestBuildResultsForProject(p, 1);
            if (build.size() == 0)
            {
                latestBuilds.add(null);
            }
            else
            {
                latestBuilds.add(build.get(0));
            }
        }

        changelists = buildManager.getLatestChangesForUser(user, 10);

        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
