package com.zutubi.pulse.cleanup;

import com.zutubi.prototype.config.CollectionListener;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.cleanup.config.CleanupWhat;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.SimpleTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class CleanupManager
{
    private static final Logger LOG = Logger.getLogger(CleanupManager.class);

    private final EventListener eventListener = new CleanupCallback();

    private EventManager eventManager;
    private Scheduler scheduler;
    private BuildManager buildManager;

    private ConfigurationRegistry configurationRegistry;
    private ConfigurationProvider configurationProvider;

    private ProjectManager projectManager;

    private static final Map<Project, Object> runningCleanups = new HashMap<Project, Object>();

    private static final String CLEANUP_NAME = "cleanup";
    private static final String CLEANUP_GROUP = "services";
    
    private static final long CLEANUP_FREQUENCY = Constants.HOUR;

    private BuildResultDao buildResultDao;

    /**
     * Initialise the cleanup manager, registering event listeners and scheduling callbacks.
     */
    public void init()
    {
        try
        {
            // register project configuration.  This will eventually be handled as an extension point
            configurationRegistry.registerProjectMapExtension("cleanup", CleanupConfiguration.class);
        }
        catch (TypeException e)
        {
            LOG.warning(e);
            return;
        }

        // register for events.
        eventManager.register(eventListener);
        
        // register for scheduled callbacks.
        Trigger trigger = scheduler.getTrigger(CLEANUP_NAME, CLEANUP_GROUP);
        if (trigger != null)
        {
            return;
        }

        // initialise the trigger.
        trigger = new SimpleTrigger(CLEANUP_NAME, CLEANUP_GROUP, CLEANUP_FREQUENCY);
        trigger.setTaskClass(CleanupBuilds.class);

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
        }

        CollectionListener<ProjectConfiguration> listener = new CollectionListener<ProjectConfiguration>("project", ProjectConfiguration.class, true)
        {
            protected void preInsert(MutableRecord record)
            {
            }

            protected void instanceInserted(ProjectConfiguration instance)
            {
                CleanupConfiguration cleanupConfiguration = new CleanupConfiguration();
                cleanupConfiguration.setName("default");
                configurationProvider.insert(PathUtils.getPath(instance.getConfigurationPath(), "cleanup"), cleanupConfiguration);
            }

            protected void instanceDeleted(ProjectConfiguration instance)
            {
            }

            protected void instanceChanged(ProjectConfiguration instance)
            {
            }
        };
        listener.register(configurationProvider);
    }

    public void cleanupBuilds()
    {
        // Lookup project cleanup info, query for old builds, cleanup where necessary
        List<Project> projects = projectManager.getProjects();
        for (Project project : projects)
        {
            cleanupBuilds(project);
        }
    }

    /**
     * Returns true if a cleanup is being run for the specified project, false otherwise.
     *
     * @param project being queried.
     *
     * @return true iff a cleanup is in progress.
     */
    public boolean isCleanupInProgress(Project project)
    {
        return runningCleanups.containsKey(project);
    }

    /**
     * Execute the configured cleanup rules for the specified project.
     *
     * @param project   the project to be cleaned up.
     */
    public void cleanupBuilds(Project project)
    {
        try
        {
            runningCleanups.put(project, null);

            ProjectConfiguration projectConfig = projectManager.getProjectConfig(project.getId());
            Map<String, CleanupConfiguration> cleanupConfigs = (Map<String, CleanupConfiguration>) projectConfig.getExtensions().get("cleanup");

            if (cleanupConfigs != null)
            {
                // if cleanup rules are specified.  Maybe we should always have at least an empty map?
                List<CleanupConfiguration> rules = new LinkedList<CleanupConfiguration>(cleanupConfigs.values());

                for (CleanupConfiguration rule : rules)
                {
                    cleanupBuilds(rule, project);
                }
            }
        }
        finally
        {
            runningCleanups.remove(project);
        }
    }

    public void cleanupBuilds(Project project, CleanupConfiguration rule)
    {
        try
        {
            runningCleanups.put(project, null);
            cleanupBuilds(rule, project);
        }
        finally
        {
            runningCleanups.remove(project);
        }
    }

    public void cleanupBuilds(User user)
    {
        int count = buildResultDao.getCompletedResultCount(user);
        int max = user.getMyBuildsCount();
        if(count > max)
        {
            List<BuildResult> results = buildResultDao.getOldestCompletedBuilds(user, count - max);
            for(BuildResult result: results)
            {
                buildManager.cleanupResult(result);
            }
        }
    }

    private synchronized void cleanupBuilds(CleanupConfiguration rule, Project project)
    {
        List<BuildResult> oldBuilds = rule.getMatchingResults(project, buildResultDao);

        for (BuildResult build : oldBuilds)
        {
            if (rule.getWhat() == CleanupWhat.WORKING_DIRECTORIES_ONLY)
            {
                buildManager.cleanupWork(build);
            }
            else
            {
                buildManager.cleanupResult(build);
            }
        }
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    /**
     * Listen for build completed events, triggering each completed builds projects
     * cleanup routines.
     * 
     */
    private class CleanupCallback implements EventListener
    {
        public void handleEvent(Event evt)
        {
            BuildCompletedEvent completedEvent = (BuildCompletedEvent) evt;
            BuildResult result = completedEvent.getResult();

            if(result.isPersonal())
            {
                cleanupBuilds(result.getUser());
            }
            else
            {
                cleanupBuilds(result.getProject());
            }
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{BuildCompletedEvent.class};
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
