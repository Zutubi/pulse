package com.zutubi.pulse.web.user;

import com.zutubi.pulse.ResultNotifier;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.core.model.NamedEntityComparator;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * Action to view the user's dashboard: their own Pulse "homepage".
 */
public class DashboardAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(DashboardAction.class);

    private User user;
    private DashboardConfiguration dashboardConfig;
    private List<BuildResult> myBuilds;
    private List<Project> shownProjects;
    private Collection<ProjectGroup> shownGroups;
    private List<Changelist> changelists;
    private List<Changelist> projectChangelists = null;

    private BuildManager buildManager;

    private List<String> contactPointsWithErrors = new LinkedList<String>();
    private BuildColumns columns;

    private ResultNotifier resultNotifier;

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
            columns = new BuildColumns(user.getPreferences().getMyProjectsColumns(), projectManager);
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

    public List<BuildResult> getChangelistResults(Changelist changelist)
    {
        try
        {
            Set<Long> ids = changelistDao.getAllAffectedResultIds(changelist);
            List<BuildResult> buildResults = new LinkedList<BuildResult>();
            for(Long id: ids)
            {
                BuildResult buildResult = buildManager.getBuildResult(id);
                if (buildResult != null)
                {
                    buildResults.add(buildResult);
                }
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
        catch (Exception e)
        {
            LOG.severe(e);
            return Collections.emptyList();
        }
    }

    public List<String> getContactPointsWithErrors()
    {
        return contactPointsWithErrors;
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUsername();
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

        for(ProjectGroup group: shownGroups)
        {
            for(Project project: group.getProjects())
            {
                shownProjects.remove(project);
            }
        }

        Collections.sort(shownProjects, new NamedEntityComparator());

        changelists = buildManager.getLatestChangesForUser(user, dashboardConfig.getMyChangeCount());
        Collections.sort(changelists, new ChangelistComparator());

        Set<Project> projects = userManager.getUserProjects(user, projectManager);
        if(projects.size() > 0 && dashboardConfig.isShowProjectChanges())
        {
            projectChangelists = buildManager.getLatestChangesForProjects(projects.toArray(new Project[]{}), dashboardConfig.getProjectChangeCount());
        }

        for(ContactConfiguration contact: user.getConfig().getPreferences().getContacts().values())
        {
            if(resultNotifier.hasError(contact))
            {
                contactPointsWithErrors.add(contact.getName());
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

    public void setResultNotifier(ResultNotifier resultNotifier)
    {
        this.resultNotifier = resultNotifier;
    }
}
