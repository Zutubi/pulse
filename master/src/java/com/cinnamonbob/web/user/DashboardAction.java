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
    private List<Project> changeProjects;
    private List<BuildResult> changeBuilds;

    private ProjectManager projectManager;
    private BuildManager buildManager;
    private UserManager userManager;

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

    public List<Project> getChangeProjects()
    {
        return changeProjects;
    }

    public List<BuildResult> getChangeBuilds()
    {
        return changeBuilds;
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return ERROR;
        }
        user = userManager.getUser(login);
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
        changeProjects = new LinkedList<Project>();
        changeBuilds = new LinkedList<BuildResult>();

        for (Changelist list : changelists)
        {
            BuildResult build = buildManager.getBuildResult(list.getResultId());
            changeProjects.add(build.getProject());
            changeBuilds.add(build);
        }

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

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
