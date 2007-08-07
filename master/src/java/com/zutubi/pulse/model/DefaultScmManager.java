package com.zutubi.pulse.model;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.SimpleTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scm.MonitorScms;
import com.zutubi.pulse.scm.ScmChangeEvent;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.scm.config.ScmConfiguration;
import com.zutubi.pulse.servercore.scm.ScmClient;
import com.zutubi.pulse.util.Pair;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Constants;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 */
public class DefaultScmManager implements ScmManager, Stoppable
{
    private static final Logger LOG = Logger.getLogger(DefaultScmManager.class);

    private ProjectManager projectManager;
    private EventManager eventManager;
    private ShutdownManager shutdownManager;

    private Scheduler scheduler;
    private static final String MONITOR_NAME = "poll";
    private static final String MONITOR_GROUP = "scm";
    private static final long POLLING_FREQUENCY = Constants.MINUTE;

    private ConfigurationProvider configurationProvider;

    private final Map<Long, Pair<Long, Revision>> waiting = new HashMap<Long, Pair<Long, Revision>>();
    private final Map<Long, Revision> latestRevisions = new HashMap<Long, Revision>();

    private ThreadPoolExecutor executor = null;

    private static final int DEFAULT_POLL_THREAD_COUNT = 10;
    private static final String PROPERTY_POLLING_THREAD_COUNT = "scm.polling.thread.count";


    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void init()
    {
        int pollThreadCount = DEFAULT_POLL_THREAD_COUNT;
        
        if (System.getProperties().contains(PROPERTY_POLLING_THREAD_COUNT))
        {
            pollThreadCount = Integer.getInteger(PROPERTY_POLLING_THREAD_COUNT);
        }

        executor = new ThreadPoolExecutor(pollThreadCount, pollThreadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        shutdownManager.addStoppable(this);

        // check if the trigger exists. if not, create and schedule.
        Trigger trigger = scheduler.getTrigger(MONITOR_NAME, MONITOR_GROUP);
        if (trigger != null)
        {
            return;
        }

        // initialise the trigger.
        trigger = new SimpleTrigger(MONITOR_NAME, MONITOR_GROUP, POLLING_FREQUENCY);
        trigger.setTaskClass(MonitorScms.class);

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
        }
    }

    public void shutdown()
    {
        if (executor != null)
        {
            executor.shutdownNow();
        }
    }


    public void stop(boolean force)
    {
        shutdown();
    }

    private List<ProjectConfiguration> getActiveProjects()
    {
        return CollectionUtils.filter(projectManager.getAllProjectConfigs(), new Predicate<ProjectConfiguration>()
        {
            public boolean satisfied(ProjectConfiguration configuration)
            {
                ScmConfiguration scm = configuration.getScm();
                return scm != null && configuration.getScm().getMonitor();
            }
        });
    }

    public void pollActiveScms()
    {
        for (final ProjectConfiguration projectConfiguration: getActiveProjects())
        {
            executor.execute(new Runnable()
            {
                public void run()
                {
                    long start = System.currentTimeMillis();
                    process(projectConfiguration);
                    long end = System.currentTimeMillis();
                    LOG.debug("Scm polling took " + ((end - start)/Constants.SECOND) + " seconds.");
                }
            });
        }

        while (executor.getActiveCount() > 0)
        {
            try
            {
                Thread.sleep(Constants.SECOND);
            }
            catch (InterruptedException e)
            {
                // noop.
                LOG.debug(e);
            }
        }
    }

    private void process(ProjectConfiguration projectConfig)
    {
        ScmConfiguration scm = projectConfig.getScm();
        Project project = projectManager.getProject(projectConfig.getProjectId());

        try
        {
            long now = System.currentTimeMillis();
            if (!checkPollingInterval(project, scm, now))
            {
                // do not poll the scm just yet.
                return;
            }

            // set the poll time.
            project.setLastPollTime(now);
            projectManager.save(project);

            // when was the last time that we checked? if never, get the latest revision.
            ScmClient client = scm.createClient();
            if (!latestRevisions.containsKey(project.getId()))
            {
                latestRevisions.put(project.getId(), client.getLatestRevision());
                return;
            }

            Revision previous = latestRevisions.get(project.getId());
            
            // slightly paranoid, but we can not rely on the scm implementations to behave as expected.
            if (previous == null)
            {
                latestRevisions.put(project.getId(), client.getLatestRevision());
                return;
            }

            // We need to move this CVS specific code into the cvs implementation specific code.
            if (scm.isQuietPeriodEnabled())
            {
                // are we waiting
                if (waiting.containsKey(project.getId()))
                {
                    long quietTime = waiting.get(project.getId()).first;
                    if (quietTime < System.currentTimeMillis())
                    {
                        Revision lastChange = waiting.get(project.getId()).second;
                        Revision latest = getLatestRevisionSince(lastChange, client);
                        if (latest != null)
                        {
                            // there has been a commit during the 'quiet period', lets reset the timer.
                            waiting.put(project.getId(), new Pair<Long, Revision>(System.currentTimeMillis() + scm.getQuietPeriod() * Constants.MINUTE, latest));
                        }
                        else
                        {
                            // there have been no commits during the 'quiet period', trigger a change.
                            sendScmChangeEvent(projectConfig, lastChange, previous);
                            waiting.remove(project.getId());
                        }
                    }
                }
                else
                {
                    Revision latest = getLatestRevisionSince(previous, client);
                    if (latest != null)
                    {
                        if (scm.getQuietPeriod() != 0)
                        {
                            waiting.put(project.getId(), new Pair<Long, Revision>(System.currentTimeMillis() + scm.getQuietPeriod() * Constants.MINUTE, latest));
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
                Revision latest = getLatestRevisionSince(previous, client);
                if (latest != null)
                {
                    sendScmChangeEvent(projectConfig, latest, previous);
                }
            }
        }
        catch (ScmException e)
        {
            // there has been a problem communicating with one of the scms. Log the
            // warning and move on.
            // This needs to be brought to the attention of the user since its likely to
            // be the result of a configuration problem.
            LOG.warning(e.getMessage(), e);
        }
    }

    private Revision getLatestRevisionSince(Revision revision, ScmClient client) throws ScmException
    {
        List<Revision> revisions = client.getRevisionsSince(revision);
        if (revisions.size() > 0)
        {
            // get the latest revision.
            return revisions.get(revisions.size() - 1);
        }
        return null;
    }

    private void sendScmChangeEvent(ProjectConfiguration projectConfig, Revision latest, Revision previous)
    {
        LOG.finer("publishing scm change event for " + projectConfig.getName() + " revision " + latest);
        eventManager.publish(new ScmChangeEvent(projectConfig, latest, previous));
        latestRevisions.put(projectConfig.getProjectId(), latest);
    }

    private boolean checkPollingInterval(Project project, ScmConfiguration scm, long now)
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

    public int getDefaultPollingInterval()
    {
        return configurationProvider.get(GeneralAdminConfiguration.class).getScmPollingInterval();
    }

    /**
     * Required resource
     *
     * @param eventManager instance
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * Required resource
     *
     * @param shutdownManager instance
     */
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
}
