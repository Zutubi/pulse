package com.zutubi.pulse.web.user;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.prototype.config.user.DashboardConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.ActionSupport;

import java.util.*;

/**
 * Action to view the user's dashboard: their own Pulse "homepage".
 */
public class DashboardAction extends ActionSupport
{
    private User user;
    private DashboardConfiguration dashboardConfig;
    private List<BuildResult> myBuilds;
    private List<Project> shownProjects;
    private Collection<ProjectGroup> shownGroups;
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

    public DashboardConfiguration getDashboardConfig()
    {
        return dashboardConfig;
    }

    public List<BuildResult> getMyBuilds()
    {
        return myBuilds;
    }

    public List<Project> getShownProjects()
    {
        return shownProjects;
    }

    public Collection<ProjectGroup> getShownGroups()
    {
        return shownGroups;
    }

    public BuildColumns getColumns()
    {
        if(columns == null)
        {
            columns = new BuildColumns(user.getPreferences().getSettings().getMyProjectsColumns(), projectManager);
        }
        return columns;
    }
    
    public List<BuildResult> getLatestBuilds(Project p)
    {
        return buildManager.getLatestBuildResultsForProject(p, dashboardConfig.getBuildCount());
    }

    public List<Changelist> getChangelists()
    {
        return changelists;
    }

    public List<Changelist> getProjectChangelists()
    {
        return projectChangelists;
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

        dashboardConfig = user.getConfig().getPreferences().getDashboard();
        myBuilds = buildManager.getPersonalBuilds(user);

        if(dashboardConfig.isShowAllProjects())
        {
            shownProjects = new LinkedList<Project>(projectManager.getProjects(false));
        }
        else
        {
            shownProjects = projectManager.mapConfigsToProjects(dashboardConfig.getShownProjects());
        }
        
        Collections.sort(shownProjects, new NamedEntityComparator());

        if (dashboardConfig.isShowAllGroups())
        {
            shownGroups = projectManager.getAllProjectGroups();
        }
        else
        {
            List<String> groupNames = dashboardConfig.getShownGroups();
            shownGroups = new ArrayList<ProjectGroup>(groupNames.size());
            for(String groupName: groupNames)
            {
                shownGroups.add(projectManager.getProjectGroup(groupName));
            }
        }

        changelists = buildManager.getLatestChangesForUser(user, dashboardConfig.getMyChangeCount());
        Collections.sort(changelists, new ChangelistComparator());

        Set<Project> projects = userManager.getUserProjects(user, projectManager);
        if(projects.size() > 0 && dashboardConfig.isShowProjectChanges())
        {
            projectChangelists = buildManager.getLatestChangesForProjects(projects.toArray(new Project[]{}), dashboardConfig.getProjectChangeCount());
        }

        for(ContactConfiguration contact: user.getConfig().getPreferences().getContacts().values())
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
        return projectManager.getProject(id, true);
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
