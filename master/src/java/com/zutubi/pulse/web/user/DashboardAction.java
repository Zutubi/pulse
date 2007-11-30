package com.zutubi.pulse.web.user;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.ActionSupport;

import java.util.*;

/**
 * Action to view the user's dashboard: their own Pulse "homepage".
 */
public class DashboardAction extends ActionSupport
{
    private User user;
    private List<BuildResult> myBuilds;
    private List<Project> shownProjects;
    private List<ProjectGroup> shownGroups;
    private List<Changelist> changelists;
    private List<Changelist> projectChangelists = null;

    private BuildManager buildManager;
    private UserManager userManager;

    private boolean contactError = false;
    private BuildColumns columns;

    public User getUser()
    {
        return user;
    }

    public List<BuildResult> getMyBuilds()
    {
        return myBuilds;
    }

    public List<Project> getShownProjects()
    {
        return shownProjects;
    }

    public List<ProjectGroup> getShownGroups()
    {
        return shownGroups;
    }

    public BuildColumns getColumns()
    {
        if(columns == null)
        {
            columns = new BuildColumns(user.getMyProjectsColumns(), projectManager);
        }
        return columns;
    }
    
    public List<BuildResult> getLatestBuilds(Project p)
    {
        return buildManager.getLatestBuildResultsForProject(p, user.getDashboardBuildCount());
    }

    public List<Changelist> getChangelists()
    {
        return changelists;
    }

    public List<Changelist> getProjectChangelists()
    {
        return projectChangelists;
    }

    public List<BuildResult> getChangelistResults(Changelist changelist)
    {
        Set<Long> ids = changelistDao.getAllAffectedResultIds(changelist);
        List<BuildResult> buildResults = new LinkedList<BuildResult>();
        for(Long id: ids)
        {
            buildResults.add(buildManager.getBuildResult(id));
        }

        Collections.sort(buildResults, new Comparator<BuildResult>()
        {
            public int compare(BuildResult b1, BuildResult b2)
            {
                NamedEntityComparator comparator = new NamedEntityComparator();
                int result = comparator.compare(b1.getProject(), b2.getProject());
                if(result == 0)
                {
                    result = (int)(b1.getNumber() - b2.getNumber());
                }

                return result;
            }
        });

        return buildResults;
    }

    public boolean isContactError()
    {
        return contactError;
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

        myBuilds = buildManager.getPersonalBuilds(user);

        if(user.getShowAllProjects())
        {
            shownProjects = projectManager.getAllProjectsCached();
        }
        else
        {
            shownProjects = new ArrayList<Project>(user.getShownProjects());
        }
        
        Collections.sort(shownProjects, new NamedEntityComparator());

        shownGroups = new ArrayList<ProjectGroup>(user.getShownGroups());
        Collections.sort(shownGroups, new NamedEntityComparator());

        changelists = buildManager.getLatestChangesForUser(user, user.getMyChangesCount());
        Collections.sort(changelists, new ChangelistComparator());

        Set<Project> projects = userManager.getUserProjects(user, projectManager);
        if(projects.size() > 0 && user.getShowProjectChanges())
        {
            projectChangelists = buildManager.getLatestChangesForProjects(projects.toArray(new Project[]{}), user.getProjectChangesCount());
        }

        for(ContactPoint contact: user.getContactPoints())
        {
            if(contact.hasError())
            {
                contactError = true;
            }
        }

        return SUCCESS;
    }

    public Project getProject(long id)
    {
        return projectManager.getProject(id);
    }

    public BuildResult getResult(long id)
    {
        return buildManager.getBuildResult(id);
    }

    public boolean canWrite(Project project)
    {
        try
        {
            projectManager.checkWrite(project);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    /**
     * Required resource.
     *
     * @param buildManager instance
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    /**
     * Required resource.
     *
     * @param userManager instance
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
