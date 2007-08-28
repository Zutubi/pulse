package com.zutubi.pulse.model;

import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.persistence.ScmDao;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.SimpleTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scm.*;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.Pair;
import com.zutubi.pulse.util.logging.Logger;

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

    private ScmDao scmDao = null;

    private EventManager eventManager;
    private ShutdownManager shutdownManager;

    private Scheduler scheduler;
    private static final String MONITOR_NAME = "poll";
    private static final String MONITOR_GROUP = "scm";
    private static final long POLLING_FREQUENCY = Constants.MINUTE;

    private MasterConfigurationManager configManager;

    private final Map<Long, Pair<Long, Revision>> waiting = new HashMap<Long, Pair<Long, Revision>>();
    private final Map<Long, Revision> latestRevisions = new HashMap<Long, Revision>();

    private ThreadPoolExecutor executor = null;

    private static final int DEFAULT_POLL_THREAD_COUNT = 10;
    private static final String PROPERTY_POLLING_THREAD_COUNT = "scm.polling.thread.count";

    public void setScmDao(ScmDao scmDao)
    {
        this.scmDao = scmDao;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setConfigurationManager(MasterConfigurationManager configManager)
    {
        this.configManager = configManager;
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

    public List<Scm> getActiveScms()
    {
        return scmDao.findAllActive();
    }

    public void pollActiveScms()
    {
        for (final Scm scm : getActiveScms())
        {
            executor.execute(new Runnable()
            {
                public void run()
                {
                    long start = System.currentTimeMillis();
                    process(scm);
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

    private void process(Scm scm)
    {
        long now = System.currentTimeMillis();
        if (!checkPollingInterval(scm, now))
        {
            // do not poll the scm just yet.
            return;
        }

        // set the poll time.
        scm.setLastPollTime(now);
        save(scm);

        SCMServer server = null;
        try
        {
            // when was the last time that we checked? if never, get the latest revision.
            server = scm.createServer();
            if (!latestRevisions.containsKey(scm.getId()))
            {
                latestRevisions.put(scm.getId(), server.getLatestRevision());
                return;
            }

            Revision previous = latestRevisions.get(scm.getId());
            
            // slightly paranoid, but we can not rely on the scm implementations to behave as expected.
            if (previous == null)
            {
                latestRevisions.put(scm.getId(), server.getLatestRevision());
                return;
            }

            // We need to move this CVS specific code into the cvs implementation specific code.
            if (scm instanceof Cvs)
            {
                Cvs cvs = (Cvs) scm;
                // are we waiting
                if (waiting.containsKey(scm.getId()))
                {
                    long quietTime = waiting.get(scm.getId()).first;
                    if (quietTime < System.currentTimeMillis())
                    {
                        Revision lastChange = waiting.get(scm.getId()).second;
                        Revision latest = getLatestRevisionSince(lastChange, server);
                        if (latest != null)
                        {
                            // there has been a commit during the 'quiet period', lets reset the timer.
                            waiting.put(scm.getId(), new Pair<Long, Revision>(System.currentTimeMillis() + cvs.getQuietPeriod(), latest));
                        }
                        else
                        {
                            // there have been no commits during the 'quiet period', trigger a change.
                            sendScmChangeEvent(scm, lastChange, previous);
                            waiting.remove(scm.getId());
                        }
                    }
                }
                else
                {
                    Revision latest = getLatestRevisionSince(previous, server);
                    if (latest != null)
                    {
                        if (cvs.getQuietPeriod() != 0)
                        {
                            waiting.put(scm.getId(), new Pair<Long, Revision>(System.currentTimeMillis() + cvs.getQuietPeriod(), latest));
                        }
                        else
                        {
                            sendScmChangeEvent(scm, latest, previous);
                        }
                    }
                }
            }
            else
            {
                Revision latest = getLatestRevisionSince(previous, server);
                if (latest != null)
                {
                    sendScmChangeEvent(scm, latest, previous);
                }
            }
        }
        catch (SCMException e)
        {
            // there has been a problem communicating with one of the scms. Log the
            // warning and move on.
            // This needs to be brought to the attention of the user since its likely to
            // be the result of a configuration problem.
            LOG.warning(e.getMessage(), e);
        }
        finally
        {
            SCMServerUtils.close(server);
        }
    }

    private Revision getLatestRevisionSince(Revision revision, SCMServer server) throws SCMException
    {
        List<Revision> revisions = server.getRevisionsSince(revision);
        if (revisions.size() > 0)
        {
            // get the latest revision.
            return revisions.get(revisions.size() - 1);
        }
        return null;
    }

    private void sendScmChangeEvent(Scm scm, Revision latest, Revision previous)
    {
        if (latest != null && previous != null && latest.compareTo(previous) == 0)
        {
            LOG.warning("Scm monitoring detected that the previous change revision '"+ previous +"' is the same as " +
                    "the latest change revision '" + latest +"', and will therefore ignore this change.");
            return;
        }

        LOG.finer("publishing scm change event for " + scm + " revision " + latest);
        eventManager.publish(new SCMChangeEvent(scm, latest, previous));
        latestRevisions.put(scm.getId(), latest);
    }

    private boolean checkPollingInterval(Scm scm, long now)
    {
        // A) is it time to poll this scm server?
        if (scm.getLastPollTime() != null)
        {
            // poll interval.
            int pollingInterval = getDefaultPollingInterval();
            if (scm.getPollingInterval() != null)
            {
                pollingInterval = scm.getPollingInterval();
            }

            long lastPollTime = scm.getLastPollTime();
            long nextPollTime = lastPollTime + Constants.MINUTE * pollingInterval;

            if (now < nextPollTime)
            {
                return false;
            }
        }
        return true;
    }


    public void save(Scm entity)
    {
        scmDao.save(entity);
    }

    public void delete(Scm entity)
    {
        scmDao.delete(entity);
    }

    public Scm getScm(long id)
    {
        return scmDao.findById(id);
    }

    public int getDefaultPollingInterval()
    {
        return configManager.getAppConfig().getScmPollingInterval();
    }

    public void setDefaultPollingInterval(int interval)
    {
        configManager.getAppConfig().setScmPollingInterval(interval);
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
}
