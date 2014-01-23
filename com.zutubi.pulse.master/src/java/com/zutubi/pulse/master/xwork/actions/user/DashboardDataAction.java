package com.zutubi.pulse.master.xwork.actions.user;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.notifications.ResultNotifier;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.project.BrowseDataAction;
import com.zutubi.pulse.master.xwork.actions.project.ProjectsModel;
import com.zutubi.pulse.master.xwork.actions.project.ProjectsModelSorter;
import com.zutubi.pulse.master.xwork.actions.project.ProjectsModelsHelper;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

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
    private ConfigurationManager configurationManager;
    private TransactionManager pulseTransactionManager;
    private HibernateTransactionManager transactionManager;

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
        // See {@link BrowseDataAction} for commentary on the use of transactions in this action.
        if (Boolean.getBoolean(BrowseDataAction.PROPERTY_DISABLE_BROWSE_CONSISTENCY))
        {
            return assembleModel();
        }
        else
        {
            return pulseTransactionManager.runInTransaction(new NullaryFunction<String>()
            {
                public String process()
                {
                    return assembleModel();
                }
            });
        }
    }

    private String assembleModel()
    {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(true);
        return template.execute(new TransactionCallback<String>()
        {
            public String doInTransaction(TransactionStatus status)
            {
                String login = SecurityUtils.getLoggedInUsername();
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
                List<UserResponsibilityModel> responsibilities = newArrayList(transform(responsibleFor, new Function<Project, UserResponsibilityModel>()
                {
                    public UserResponsibilityModel apply(Project project)
                    {
                        return new UserResponsibilityModel(project.getName(), project.getId());
                    }
                }));

                final DashboardConfiguration dashboardConfig = user.getConfig().getPreferences().getDashboard();

                Predicate<Project> projectPredicate;
                ProjectsModelSorter sorter = new ProjectsModelSorter();

                if (dashboardConfig.isShowAllProjects())
                {
                    projectPredicate = Predicates.alwaysTrue();
                }
                else
                {
                    projectPredicate = new Predicate<Project>()
                    {
                        public boolean apply(Project project)
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
                        groupPredicate = Predicates.alwaysTrue();
                    }
                    else
                    {
                        groupPredicate = new Predicate<ProjectGroup>()
                        {
                            public boolean apply(ProjectGroup projectGroup)
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
                    groupPredicate = Predicates.<ProjectGroup>alwaysFalse();
                }

                ProjectsModelsHelper helper = objectFactory.buildBean(ProjectsModelsHelper.class);
                Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
                List<ProjectsModel> projectsModels = helper.createProjectsModels(user, dashboardConfig, user.getDashboardCollapsed(), urls, projectPredicate, groupPredicate, showUngrouped);
                sorter.sort(projectsModels);

                List<PersistentChangelist> changelists = changelistManager.getLatestChangesForUser(user, dashboardConfig.getMyChangeCount());
                Collections.sort(changelists, new ChangelistComparator());

                Set<Project> projects = userManager.getUserProjects(user, projectManager);
                List<PersistentChangelist> projectChangelists = new LinkedList<PersistentChangelist>();
                if (projects.size() > 0 && dashboardConfig.isShowProjectChanges())
                {
                    projectChangelists.addAll(changelistManager.getLatestChangesForProjects(projects.toArray(new Project[projects.size()]), dashboardConfig.getProjectChangeCount()));
                }

                model = new DashboardModel(contactPointsWithErrors, responsibilities, user.getDashboardFilter(), projectsModels, mapChangelists(changelists), mapChangelists(projectChangelists));
                return SUCCESS;
            }
        });
    }

    private List<ChangelistModel> mapChangelists(List<PersistentChangelist> changelists)
    {
        return newArrayList(transform(changelists, new Function<PersistentChangelist, ChangelistModel>()
        {
            public ChangelistModel apply(PersistentChangelist persistentChangelist)
            {
                return new ChangelistModel(persistentChangelist, getChangeUrl(persistentChangelist), getChangelistResults(persistentChangelist), getCommitMessageSupport(persistentChangelist));
            }
        }));
    }

    private String getChangeUrl(PersistentChangelist changelist)
    {
        // We cache the URL as velocity null handling is brain dead
        try
        {
            if (changelist != null)
            {
                Revision revision = changelist.getRevision();
                if (revision != null)
                {
                    for (long id: changelistManager.getAffectedProjectIds(changelist))
                    {
                        try
                        {
                            ProjectConfiguration p = getProjectManager().getProjectConfig(id, false);
                            if (p != null && p.getChangeViewer() != null)
                            {
                                String url = p.getChangeViewer().getRevisionURL(p, revision);
                                if(url != null)
                                {
                                    return url;
                                }
                            }
                        }
                        catch (AccessDeniedException e)
                        {
                            // User cannot view this project
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }

        return null;
    }

    private List<BuildResult> getChangelistResults(PersistentChangelist changelist)
    {
        try
        {
            Set<Long> ids = changelistManager.getAffectedBuildIds(changelist);
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

            Collections.sort(buildResults, new BuildResult.CompareByOwnerThenNumber());

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

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setPulseTransactionManager(TransactionManager pulseTransactionManager)
    {
        this.pulseTransactionManager = pulseTransactionManager;
    }

    public void setTransactionManager(HibernateTransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
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
