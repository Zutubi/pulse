/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.ProjectNameComparator;
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
        Collections.sort(projects, new ProjectNameComparator());
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

    /**
     * Compare changelists based on two things:
     * - sort primarily by build id
     * - sort secondarily by revision
     * Note we sort in descending order.
     */
    private class ChangelistComparator implements Comparator<Changelist>
    {
        public int compare(Changelist c1, Changelist c2)
        {
            long id1 = c1.getResultId();
            long id2 = c2.getResultId();

            if (id1 < id2)
            {
                return 1;
            }
            else if (id1 > id2)
            {
                return -1;
            }

            // Need to go to the revision.
            return -c1.getRevision().compareTo(c2.getRevision());
        }
    }
}
