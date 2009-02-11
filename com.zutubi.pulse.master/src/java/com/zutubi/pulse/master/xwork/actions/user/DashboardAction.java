package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.core.model.NamedEntityComparator;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.ResultNotifier;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.project.ProjectsModel;
import com.zutubi.pulse.master.xwork.actions.project.ProjectsModelsHelper;
import com.zutubi.util.FalsePredicate;
import com.zutubi.util.Predicate;
import com.zutubi.util.TruePredicate;
import com.zutubi.util.bean.ObjectFactory;
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
    private List<ProjectsModel> models;
    private List<PersistentChangelist> changelists;
    private List<PersistentChangelist> projectChangelists = null;

    private BuildManager buildManager;

    private List<String> contactPointsWithErrors = new LinkedList<String>();

    private ResultNotifier resultNotifier;
    private ObjectFactory objectFactory;

    public User getUser()
    {
        return user;
    }

    public DashboardConfiguration getDashboardConfig()
    {
        return dashboardConfig;
    }

    public List<ProjectsModel> getModels()
    {
        return models;
    }
    
    public List<String> getColumns()
    {
        return dashboardConfig.getColumns();
    }
    
    public List<PersistentChangelist> getChangelists()
    {
        return changelists;
    }

    public List<PersistentChangelist> getProjectChangelists()
    {
        return projectChangelists;
    }

    public List<BuildResult> getChangelistResults(PersistentChangelist changelist)
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

    public String execute()
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

        Predicate<Project> projectPredicate;
        if (dashboardConfig.isShowAllProjects())
        {
            projectPredicate = new TruePredicate<Project>();
        }
        else
        {
            projectPredicate = new Predicate<Project>()
            {
                public boolean satisfied(Project project)
                {
                    return dashboardConfig.getShownProjects().contains(project.getConfig());
                }
            };
        }

        boolean showUngrouped;
        Predicate<ProjectGroup> groupPredicate;
        if (dashboardConfig.isGroupsShown())
        {
            showUngrouped = dashboardConfig.isShowUngrouped();
            if (dashboardConfig.isShowAllGroups())
            {
                groupPredicate = new TruePredicate<ProjectGroup>();
            }
            else
            {
                groupPredicate = new Predicate<ProjectGroup>()
                {
                    public boolean satisfied(ProjectGroup projectGroup)
                    {
                        return dashboardConfig.getShownGroups().contains(projectGroup.getName());
                    }
                };
            }
        }
        else
        {
            showUngrouped = true;
            groupPredicate = new FalsePredicate<ProjectGroup>();
        }

        ProjectsModelsHelper helper = objectFactory.buildBean(ProjectsModelsHelper.class);
        models = helper.createProjectsModels(dashboardConfig, projectPredicate, groupPredicate, showUngrouped);

        changelists = buildManager.getLatestChangesForUser(user, dashboardConfig.getMyChangeCount());
        Collections.sort(changelists, new ChangelistComparator());

        Set<Project> projects = userManager.getUserProjects(user, projectManager);
        if(projects.size() > 0 && dashboardConfig.isShowProjectChanges())
        {
            projectChangelists = buildManager.getLatestChangesForProjects(projects.toArray(new Project[projects.size()]), dashboardConfig.getProjectChangeCount());
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

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setResultNotifier(ResultNotifier resultNotifier)
    {
        this.resultNotifier = resultNotifier;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
