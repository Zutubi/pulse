package com.zutubi.pulse.web.user;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Action to view the user's dashboard: their own Pulse "homepage".
 */
public class DashboardAction extends ActionSupport
{
    private User user;
    private List<Project> projects;
    private List<BuildResult> latestBuilds;
    private List<Changelist> changelists;

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

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return "guest";
        }
        user = userManager.getUser(login);
        if (user == null)
        {
            return ERROR;
        }

        if(user.getShowAllProjects())
        {
            projects = projectManager.getAllProjects();
        }
        else
        {
            projects = userManager.getDashboardProjects(user);
        }
        
        Collections.sort(projects, new NamedEntityComparator());
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
        Collections.sort(changelists, new ChangelistComparator());

        return SUCCESS;
    }

    /**
     * Allow the template access to a specific project instance.
     *
     * @param id uniquely identifies a project.
     *
     * @return the project associated with the id, or null if it does
     * not exist.
     */
    public Project getProject(long id)
    {
        return projectManager.getProject(id);
    }

    /**
     * Allow the template access to a specific build result instance.
     *
     * @param id uniquely identifies a project.
     *
     * @return the build result associated with the id, or null if it does
     * not exist.
     */
    public BuildResult getResult(long id)
    {
        return buildManager.getBuildResult(id);
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

    private class ChangelistComparator implements Comparator<Changelist>
    {
        public int compare(Changelist c1, Changelist c2)
        {
            // Compare the date.
            return -c1.getDate().compareTo(c2.getDate());
        }
    }
}
