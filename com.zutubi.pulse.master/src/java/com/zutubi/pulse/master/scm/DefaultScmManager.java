package com.zutubi.pulse.master.scm;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.Pollable;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scheduling.SimpleTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.*;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class DefaultScmManager implements ScmManager
{
    private static final Logger LOG = Logger.getLogger(DefaultScmManager.class);

    private static final String MONITOR_NAME = "poll";
    private static final String MONITOR_GROUP = "scm";
    private static final long POLLING_FREQUENCY = Constants.MINUTE;

    private ProjectManager projectManager;
    private EventManager eventManager;
    private ShutdownManager shutdownManager;
    private ScmClientFactory<ScmConfiguration> scmClientFactory;
    private ScmContextFactory scmContextFactory;
    private Scheduler scheduler;
    private ConfigurationProvider configurationProvider;
    private ThreadFactory threadFactory;

    private final Map<Long, Pair<Long, Revision>> waiting = new HashMap<Long, Pair<Long, Revision>>();
    private final Map<Long, Revision> latestRevisions = new HashMap<Long, Revision>();

    private ThreadPoolExecutor pollingExecutor = null;

    private static final int DEFAULT_POLL_THREAD_COUNT = 10;
    private static final String PROPERTY_POLLING_THREAD_COUNT = "scm.polling.thread.count";

    public void init()
    {
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                initialise();
            }
        });

        shutdownManager.addStoppable(new Stoppable()
        {
            public void stop(boolean force)
            {
                if (pollingExecutor != null)
                {
                    pollingExecutor.shutdown();
                }
            }
        });
    }

    private void initialise()
    {
        // initialise the thread pool that runs the scm polling.
        int pollThreadCount = Integer.getInteger(PROPERTY_POLLING_THREAD_COUNT, DEFAULT_POLL_THREAD_COUNT);
        pollingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(pollThreadCount, threadFactory);

        try
        {
            // check if the trigger exists. if not, create and schedule.
            Trigger trigger = scheduler.getTrigger(MONITOR_NAME, MONITOR_GROUP);
            if (trigger == null)
            {
                trigger = new SimpleTrigger(MONITOR_NAME, MONITOR_GROUP, POLLING_FREQUENCY);
                trigger.setTaskClass(MonitorScms.class);
            }

            if (!trigger.isScheduled())
            {
                scheduler.schedule(trigger);
            }
        }
        catch (SchedulingException e)
        {
            LOG.severe("Failed to schedule scm polling trigger. Cause: " + e.getMessage() + ".  " +
                    "No scm polling is available.  See log for details.");
            LOG.severe(e);
        }
    }

    private List<ProjectConfiguration> getActiveProjects()
    {
        List<Project> filteredProjects = CollectionUtils.filter(projectManager.getProjects(false), new Predicate<Project>()
        {
            public boolean satisfied(Project project)
            {
                if (project == null || !project.getState().isInitialised())
                {
                    return false;
                }

                ScmConfiguration scm = project.getConfig().getScm();
                // check a) sanity and b) pollability.
                if (scm == null || !(scm instanceof Pollable))
                {
                    return false;
                }

                // check c) monitoring is enabled.
                return ((Pollable) scm).isMonitor();
            }
        });
        
        return CollectionUtils.map(filteredProjects, new Mapping<Project, ProjectConfiguration>()
        {
            public ProjectConfiguration map(Project project)
            {
                return project.getConfig();
            }
        });
    }

    public void pollActiveScms()
    {
        for (final ProjectConfiguration project : getActiveProjects())
        {
            pollingExecutor.execute(new Runnable()
            {
                public void run()
                {
                    process(project);
                }
            });
        }

        // wait until the polling is complete becore exiting.  This way we keep the
        // polling task running, blocking it from running again until this is complete.
        while (pollingExecutor.getActiveCount() > 0)
        {
            try
            {
                Thread.sleep(Constants.SECOND);
            }
            catch (InterruptedException e)
            {
                // noop.
            }
        }
    }

    private void process(ProjectConfiguration projectConfig)
    {
        Pollable pollable = (Pollable) projectConfig.getScm();
        long projectId = projectConfig.getProjectId();
        Project project = projectManager.getProject(projectId, false);
        ScmClient client = null;

        try
        {
            long now = System.currentTimeMillis();
            if (!checkPollingInterval(project, pollable, now))
            {
                // do not poll the scm just yet.
                return;
            }

            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Polling SCM for changes..."));
            projectManager.updateLastPollTime(projectId, now);

            // when was the last time that we checked? if never, get the latest revision.
            ScmContext context = scmContextFactory.createContext(projectConfig);
            client = scmClientFactory.createClient(projectConfig.getScm());
            if (!latestRevisions.containsKey(projectId))
            {
                Revision revision = client.getLatestRevision(context);
                latestRevisions.put(projectId, revision);
                eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Retrieved initial revision: " + revision.getRevisionString() + " (took " + TimeStamps.getPrettyElapsed(System.currentTimeMillis() - now) + ")."));
                return;
            }

            Revision previous = latestRevisions.get(projectId);

            // slightly paranoid, but we can not rely on the scm implementations to behave as expected.
            if (previous == null)
            {
                Revision revision = client.getLatestRevision(context);
                latestRevisions.put(projectId, revision);
                eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Forced initial revision: " + revision.getRevisionString() + " (took " + TimeStamps.getPrettyElapsed(System.currentTimeMillis() - now) + ")."));
                return;
            }

            if (pollable.isQuietPeriodEnabled())
            {
                // are we waiting
                if (waiting.containsKey(projectId))
                {
                    long quietTime = waiting.get(projectId).first;
                    if (quietTime < System.currentTimeMillis())
                    {
                        Revision lastChange = waiting.get(projectId).second;
                        Revision latest = getLatestRevisionSince(lastChange, client, context);
                        if (latest != null)
                        {
                            // there has been a commit during the 'quiet period', lets reset the timer.
                            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Changes detected during quiet period, restarting wait..."));
                            waiting.put(projectId, new Pair<Long, Revision>(System.currentTimeMillis() + pollable.getQuietPeriod() * Constants.MINUTE, latest));
                        }
                        else
                        {
                            // there have been no commits during the 'quiet period', trigger a change.
                            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Quiet period completed without additional changes."));
                            sendScmChangeEvent(projectConfig, lastChange, previous);
                            waiting.remove(projectId);
                        }
                    }
                    else
                    {
                        eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Still within quiet period."));
                    }
                }
                else
                {
                    Revision latest = getLatestRevisionSince(previous, client, context);
                    if (latest != null)
                    {
                        if (pollable.getQuietPeriod() != 0)
                        {
                            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Changes detected, starting quiet period wait..."));
                            waiting.put(projectId, new Pair<Long, Revision>(System.currentTimeMillis() + pollable.getQuietPeriod() * Constants.MINUTE, latest));
                        }
                        else
                        {
                            sendScmChangeEvent(projectConfig, latest, previous);
                        }
                    }
                }
            }
            else
            {
                Revision latest = getLatestRevisionSince(previous, client, context);
                if (latest != null)
                {
                    sendScmChangeEvent(projectConfig, latest, previous);
                }
            }

            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "SCM polling completed (took " + TimeStamps.getPrettyElapsed(System.currentTimeMillis() - now) + ")."));
        }
        catch (ScmException e)
        {
            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Error polling SCM: " + e.getMessage()));
            LOG.debug(e);
        }
        finally
        {
            IOUtils.close(client);
        }
    }

    private Revision getLatestRevisionSince(Revision revision, ScmClient client, ScmContext context) throws ScmException
    {
        // this assumes that getting the revision since revision x is more efficient than getting the latest revision.
        List<Revision> revisions = client.getRevisions(context, revision, null);
        if (revisions.size() > 0)
        {
            // get the latest revision.
            return revisions.get(revisions.size() - 1);
        }
        return null;
    }

    private void sendScmChangeEvent(ProjectConfiguration projectConfig, Revision latest, Revision previous)
    {
        eventManager.publish(new ScmChangeEvent(projectConfig, latest, previous));
        latestRevisions.put(projectConfig.getProjectId(), latest);
    }

    private boolean checkPollingInterval(Project project, Pollable scm, long now)
    {
        // A) is it time to poll this scm server?
        if (project.getLastPollTime() != null)
        {
            // poll interval.
            int pollingInterval = getDefaultPollingInterval();
            if (scm.isCustomPollingInterval())
            {
                pollingInterval = scm.getPollingInterval();
            }

            long lastPollTime = project.getLastPollTime();
            long nextPollTime = lastPollTime + Constants.MINUTE * pollingInterval;

            if (now < nextPollTime)
            {
                return false;
            }
        }
        return true;
    }

    public ScmContext createContext(ProjectConfiguration projectConfiguration) throws ScmException
    {
        return scmContextFactory.createContext(projectConfiguration);
    }

    public ScmClient createClient(ScmConfiguration config) throws ScmException
    {
        return scmClientFactory.createClient(config);
    }

    public int getDefaultPollingInterval()
    {
        return configurationProvider.get(GlobalConfiguration.class).getScmPollingInterval();
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setScmClientFactory(ScmClientFactory<ScmConfiguration> scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setScmContextFactory(ScmContextFactory scmContextFactory)
    {
        this.scmContextFactory = scmContextFactory;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
