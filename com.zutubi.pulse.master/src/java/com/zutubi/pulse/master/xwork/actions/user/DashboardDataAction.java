package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.core.model.NamedEntityComparator;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.notifications.ResultNotifier;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.project.ProjectsModel;
import com.zutubi.pulse.master.xwork.actions.project.ProjectsModelSorter;
import com.zutubi.pulse.master.xwork.actions.project.ProjectsModelsHelper;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.util.*;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.springframework.security.AccessDeniedException;

import java.util.*;

/**
 * Action to view the user's dashboard: their own Pulse "homepage".
 */
public class DashboardDataAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(DashboardDataAction.class);

    private User user;

    private DashboardModel model;

    private BuildManager buildManager;
    private ResultNotifier resultNotifier;
    private ObjectFactory objectFactory;
    private ConfigurationManager configurationManager;
    private TransactionManager pulseTransactionManager;
    
    public User getUser()
    {
        return user;
    }

    public DashboardModel getModel()
    {
        return model;
    }

    public String execute() throws Exception
    {
        return pulseTransactionManager.runInTransaction(new NullaryFunction<String>()
        {
            public String process()
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
    
                List<String> contactPointsWithErrors = new LinkedList<String>();
                for (ContactConfiguration contact : user.getConfig().getPreferences().getContacts().values())
                {
                    if (resultNotifier.hasError(contact))
                    {
                        contactPointsWithErrors.add(contact.getName());
                    }
                }
    
                List<Project> responsibleFor = projectManager.findByResponsible(user);
                List<ResponsibilityModel> responsibilities = CollectionUtils.map(responsibleFor, new Mapping<Project, ResponsibilityModel>()
                {
                    public ResponsibilityModel map(Project project)
                    {
                        return new ResponsibilityModel(project.getName(), project.getId());
                    }
                });
    
                final DashboardConfiguration dashboardConfig = user.getConfig().getPreferences().getDashboard();
    
                Predicate<Project> projectPredicate;
                ProjectsModelSorter sorter = new ProjectsModelSorter();
    
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
                    if (!dashboardConfig.isSortProjectsAlphabetically())
                    {
                        sorter.setProjectNameComparator(new DashboardConfigurationProjectComparator(dashboardConfig));
                        sorter.sortTemplatesToStart();
                    }
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
                        if (!dashboardConfig.isSortGroupsAlphabetically())
                        {
                            sorter.setGroupNameComparator(new DashboardConfigurationProjectGroupComparator(dashboardConfig));
                        }
                    }
                }
                else
                {
                    showUngrouped = true;
                    groupPredicate = new FalsePredicate<ProjectGroup>();
                }
    
                ProjectsModelsHelper helper = objectFactory.buildBean(ProjectsModelsHelper.class);
                Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
                List<ProjectsModel> projectsModels = helper.createProjectsModels(user, dashboardConfig, user.getDashboardCollapsed(), urls, projectPredicate, groupPredicate, showUngrouped);
                sorter.sort(projectsModels);
    
                List<PersistentChangelist> changelists = buildManager.getLatestChangesForUser(user, dashboardConfig.getMyChangeCount());
                Collections.sort(changelists, new ChangelistComparator());
    
                Set<Project> projects = userManager.getUserProjects(user, projectManager);
                List<PersistentChangelist> projectChangelists = new LinkedList<PersistentChangelist>();
                if (projects.size() > 0 && dashboardConfig.isShowProjectChanges())
                {
                    projectChangelists.addAll(buildManager.getLatestChangesForProjects(projects.toArray(new Project[projects.size()]), dashboardConfig.getProjectChangeCount()));
                }
    
                model = new DashboardModel(contactPointsWithErrors, responsibilities, projectsModels, mapChangelists(changelists), mapChangelists(projectChangelists));
    
                return SUCCESS;
            }
        });
    }

    private List<ChangelistModel> mapChangelists(List<PersistentChangelist> changelists)
    {
        return CollectionUtils.map(changelists, new Mapping<PersistentChangelist, ChangelistModel>()
        {
            public ChangelistModel map(PersistentChangelist persistentChangelist)
            {
                updateChangeUrl(persistentChangelist);
                return new ChangelistModel(persistentChangelist, getChangeUrl(), getChangelistResults(persistentChangelist), getCommitMessageSupport(persistentChangelist));
            }
        });
    }

    private List<BuildResult> getChangelistResults(PersistentChangelist changelist)
    {
        try
        {
            Set<Long> ids = changelistDao.getAllAffectedResultIds(changelist);
            List<BuildResult> buildResults = new LinkedList<BuildResult>();
            for (Long id : ids)
            {
                try
                {
                    BuildResult buildResult = buildManager.getBuildResult(id);
                    if (buildResult != null)
                    {
                        buildResults.add(buildResult);
                    }
                }
                catch (AccessDeniedException e)
                {
                    // User can't view this project, carry on with others.
                }
            }

            Collections.sort(buildResults, new Comparator<BuildResult>()
            {
                public int compare(BuildResult b1, BuildResult b2)
                {
                    NamedEntityComparator comparator = new NamedEntityComparator();
                    int result = comparator.compare(b1.getProject(), b2.getProject());
                    if (result == 0)
                    {
                        result = (int) (b1.getNumber() - b2.getNumber());
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

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setPulseTransactionManager(TransactionManager pulseTransactionManager)
    {
        this.pulseTransactionManager = pulseTransactionManager;
    }

    /**
     * A comparator that will order projects based on the order defined in the shownProjects
     * list in the dashboard configuration
     */
    private class DashboardConfigurationProjectComparator implements Comparator<String>
    {
        private Map<String, Integer> projectIndicies;

        private DashboardConfigurationProjectComparator(DashboardConfiguration configuration)
        {
            projectIndicies = new HashMap<String, Integer>();
            int index = 0;
            for (ProjectConfiguration projectConfig : configuration.getShownProjects())
            {
                projectIndicies.put(projectConfig.getName(), index);
                index++;
            }
        }

        public int compare(String o1, String o2)
        {
            if (!projectIndicies.containsKey(o1))
            {
                return 1;
            }
            if (!projectIndicies.containsKey(o2))
            {
                return -1;
            }
            int indexA = projectIndicies.get(o1);
            int indexB = projectIndicies.get(o2);
            return indexA - indexB;
        }
    }

    /**
     * A comparator that will order project groups based on the order defined in the shownProjectGroups
     * list in the dashboard configuration
     */
    private class DashboardConfigurationProjectGroupComparator implements Comparator<String>
    {
        private Map<String, Integer> groupIndicies;

        private DashboardConfigurationProjectGroupComparator(DashboardConfiguration config)
        {
            groupIndicies = new HashMap<String, Integer>();
            int index = 0;
            for (String groupName : config.getShownGroups())
            {
                groupIndicies.put(groupName, index);
                index++;
            }
        }

        public int compare(String o1, String o2)
        {
            if (!groupIndicies.containsKey(o1))
            {
                return 1;
            }
            if (!groupIndicies.containsKey(o2))
            {
                return -1;
            }
            int indexA = groupIndicies.get(o1);
            int indexB = groupIndicies.get(o2);
            return  indexA - indexB;
        }
    }
}
