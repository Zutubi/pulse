package com.zutubi.pulse.master.agent;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.events.HostPingEvent;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scheduling.SimpleTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.scheduling.tasks.PingSlaves;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the task of pinging hosts and sending out the results as events.
 * Duplicate ping requests are filtered, and ping timeouts are managed (ping
 * results are sent by the timeout even if the pinging thread is still
 * awaiting a response from a remote agent).
 */
public class HostPingService extends BackgroundServiceSupport
{
    public static final String PROPERTY_AGENT_PING_INTERVAL = "pulse.agent.ping.interval";
    public static final String PROPERTY_AGENT_PING_TIMEOUT = "pulse.agent.ping.timeout";
    public static final String PROPERTY_AGENT_LOG_TIMEOUTS = "pulse.agent.log.timeouts";

    private static final String PING_NAME = "ping";
    private static final String PING_GROUP = "services";

    private static final Logger LOG = Logger.getLogger(HostPingService.class);
    private final int masterBuildNumber = Version.getVersion().getBuildNumberAsInt();
    private Lock inProgressLock = new ReentrantLock();
    private Set<Long> inProgress = new HashSet<Long>();
    private EventManager eventManager;
    private MasterLocationProvider masterLocationProvider;
    private Scheduler scheduler;

    public HostPingService()
    {
        super("Agent Ping");
    }

    public void init()
    {
        super.init();

        // register a schedule for pinging the slaves.
        // check if the trigger exists. if not, create and schedule.
        Trigger trigger = scheduler.getTrigger(PING_NAME, PING_GROUP);
        if (trigger != null)
        {
            return;
        }

        // initialise the trigger.
        trigger = new SimpleTrigger(PING_NAME, PING_GROUP, HostPingService.getAgentPingInterval() * Constants.SECOND);
        trigger.setTaskClass(PingSlaves.class);

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
        }
    }

    public static int getAgentPingInterval()
    {
        return Integer.getInteger(PROPERTY_AGENT_PING_INTERVAL, 60);
    }

    public static int getAgentPingTimeout()
    {
        return Integer.getInteger(PROPERTY_AGENT_PING_TIMEOUT, 45);
    }

    /**
     * Requests that the given host is pinged.  When the ping completes an
     * {@link com.zutubi.pulse.master.events.HostPingEvent} is raised.
     *
     * If there is currently a ping in progress for the agent, this request
     * is ignored.  However, it is guaranteed that a ping event will be
     * raised after this method is called.
     *
     * @param host        the host to ping
     * @param hostService service for communicating with the host
     * @return true if the request was submitted, false if it was filtered
     *         due to a pending request for the same agent
     */
    public boolean requestPing(Host host, HostService hostService)
    {
        inProgressLock.lock();
        try
        {
            // Ignore duplicate requests.  If there is ping result pending
            // for this agent, don't request another one.
            if (isPingInProgress(host))
            {
                return false;
            }
            else
            {
                pingStarted(host);
                enqueueRequest(host, hostService);
                return true;
            }
        }
        finally
        {
            inProgressLock.unlock();
        }
    }

    public boolean isPingInProgress(Host host)
    {
        return inProgress.contains(host.getId());
    }

    private void pingStarted(Host host)
    {
        inProgress.add(host.getId());
    }

    private void pingCompleted(Host host)
    {
        inProgressLock.lock();
        try
        {
            inProgress.remove(host.getId());
        }
        finally
        {
            inProgressLock.unlock();
        }
    }

    private void enqueueRequest(final Host host, HostService hostService)
    {
        // Directly submit the ping to the pool for execution.  Note that
        // this thread may be stuck until a network timeout.
        ExecutorService threadPool = getExecutorService();
        final Future<HostStatus> future = threadPool.submit(new HostPing(host, hostService, masterBuildNumber, masterLocationProvider.getMasterUrl()));

        // Run a second thread to wait for up to the agent ping timeout for
        // the result of the ping.  This way we can send out the ping event
        // after at most the agent timeout period, even when the original
        // thread is still waiting on the network.
        threadPool.execute(new Runnable()
        {
            public void run()
            {
                HostStatus status;
                try
                {
                    status = future.get(getAgentPingTimeout(), TimeUnit.SECONDS);
                }
                catch (TimeoutException e)
                {
                    if (Boolean.getBoolean(PROPERTY_AGENT_LOG_TIMEOUTS))
                    {
                        LOG.warning("Timed out pinging host '" + host.getLocation() + "'", e);
                    }
                    
                    status = new HostStatus(PingStatus.OFFLINE, "Host ping timed out");
                }
                catch (Exception e)
                {
                    LOG.debug(e);

                    String message = "Unexpected error pinging host '" + host.getLocation() + "': " + e.getMessage();
                    LOG.warning(message);
                    status = new HostStatus(PingStatus.OFFLINE, message);
                }

                pingCompleted(host);
                eventManager.publish(new HostPingEvent(this, host, status));
            }
        });
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
