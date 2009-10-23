package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.events.RecipeStatusEvent;
import com.zutubi.pulse.master.agent.*;
import com.zutubi.pulse.master.events.AgentAvailableEvent;
import com.zutubi.pulse.master.events.AgentConnectivityEvent;
import com.zutubi.pulse.master.events.AgentOnlineEvent;
import com.zutubi.pulse.master.events.AgentResourcesDiscoveredEvent;
import com.zutubi.pulse.master.events.build.BuildRevisionUpdatedEvent;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.config.events.PostSaveEvent;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A recipe queue that runs an independent thread to manage the dispatching
 * of recipes.
 */
public class ThreadedRecipeQueue implements Runnable, RecipeQueue, EventListener, Stoppable, ConfigurationEventListener
{
    private static final Logger LOG = Logger.getLogger(ThreadedRecipeQueue.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition lockCondition = lock.newCondition();

    /**
     * The internal queue of assignment requests.
     */
    private final RequestQueue requestQueue = new RequestQueue();
    
    private ExecutorService executor;

    private boolean stopRequested = false;
    private boolean isRunning = false;
    /**
     * Maximum number of milliseconds between checks of the queue.  Usually
     * checks occur due to the condition being flagged, but we need to
     * wake up periodically to enforce timeouts.
     */
    private long sleepInterval = 60 * Constants.SECOND;
    /**
     * Maximum number of milliseconds to leave a request that cannot be satisfied
     * in the queue.  This is based on how long there has been no capable
     * agent available (not on how long the request has been queued).  If the
     * timeout is 0, unsatisfiable requests will be rejected immediately.  If
     * the timeout is negative, requests will never time out.
     */
    private long unsatisfiableTimeout = 0;

    private AgentManager agentManager;
    private EventManager eventManager;
    private GlobalConfiguration globalConfiguration;
    private ThreadFactory threadFactory;
    private AgentSorter agentSorter = new DefaultAgentSorter();

    public ThreadedRecipeQueue()
    {

    }

    public void init()
    {
        try
        {
            // Get all agents
            for (Agent a : agentManager.getOnlineAgents())
            {
                updateTimeoutsForAgent(a);
            }

            start();
        }
        catch (Exception e)
        {
            LOG.error(e);
        }
    }

    public void start()
    {
        if (isRunning())
        {
            throw new IllegalStateException("The queue is already running.");
        }
        LOG.debug("start();");
        executor = Executors.newSingleThreadExecutor(threadFactory);
        executor.execute(this);
    }

    public void stop()
    {
        stop(true);
    }

    /**
     * Enqueue a new recipe dispatch request.
     *
     * @param assignmentRequest the request to be enqueued
     */
    public void enqueue(RecipeAssignmentRequest assignmentRequest)
    {
        RecipeErrorEvent error = null;

        try
        {
            lock.lock();
            try
            {
                if (requestMayBeFulfilled(assignmentRequest))
                {
                    addToQueue(assignmentRequest);
                }
                else
                {
                    if(unsatisfiableTimeout == 0)
                    {
                        error = new RecipeErrorEvent(this, assignmentRequest.getRequest().getId(), "No online agent is capable of executing the build stage");
                    }
                    else
                    {
                        if(unsatisfiableTimeout > 0)
                        {
                            assignmentRequest.setTimeout(System.currentTimeMillis() + unsatisfiableTimeout);
                        }

                        addToQueue(assignmentRequest);
                    }
                }
            }
            finally
            {
                lock.unlock();
            }
        }
        catch (Exception e)
        {
            LOG.error(e);
            error = new RecipeErrorEvent(this, assignmentRequest.getRequest().getId(), "Unable to determine revision to build: " + e.getMessage());
        }


        if (error != null)
        {
            // Publish outside the lock.
            eventManager.publish(error);
        }
    }

    private void addToQueue(RecipeAssignmentRequest assignmentRequest)
    {
        requestQueue.add(assignmentRequest);
        assignmentRequest.queued();
        lockCondition.signal();
    }

    public List<RecipeAssignmentRequest> takeSnapshot()
    {
        return requestQueue.snapshot();
    }

    public boolean cancelRequest(long id)
    {
        boolean removed = false;

        try
        {
            lock.lock();
            RecipeAssignmentRequest removeRequest = null;

            for (RecipeAssignmentRequest request : requestQueue)
            {
                if (request.getRequest().getId() == id)
                {
                    removeRequest = request;
                    break;
                }
            }

            if (removeRequest != null)
            {
                requestQueue.remove(removeRequest);
                removed = true;
            }
        }
        finally
        {
            lock.unlock();
        }

        return removed;
    }

    void updateTimeoutsForAgent(Agent agent)
    {
        lock.lock();
        try
        {
            resetTimeouts(agent);
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    private void resetTimeouts(Agent agent)
    {
        for(RecipeAssignmentRequest request: requestQueue)
        {
            if(request.hasTimeout() && request.getHostRequirements().fulfilledBy(request, agent.getService()))
            {
                request.clearTimeout();
            }
        }
    }

    void offline()
    {
        List<RecipeAssignmentRequest> removedRequests = null;

        lock.lock();
        try
        {

            if(unsatisfiableTimeout == 0)
            {
                removedRequests = removeUnfulfillable();
            }
            else if(unsatisfiableTimeout > 0)
            {
                checkQueuedTimeouts(System.currentTimeMillis() + unsatisfiableTimeout);
            }

            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }

        if(removedRequests != null)
        {
            publishUnfulfillable(removedRequests);
        }
    }

    private void checkQueuedTimeouts(long timeout)
    {
        assert(lock.isHeldByCurrentThread());

        for (RecipeAssignmentRequest request : requestQueue)
        {
            if (!request.hasTimeout() && !requestMayBeFulfilled(request))
            {
                request.setTimeout(timeout);
            }
        }
    }

    private List<RecipeAssignmentRequest> removeUnfulfillable()
    {
        assert(lock.isHeldByCurrentThread());

        List<RecipeAssignmentRequest> unfulfillable = new LinkedList<RecipeAssignmentRequest>();
        for (RecipeAssignmentRequest request : requestQueue)
        {
            if (!requestMayBeFulfilled(request))
            {
                unfulfillable.add(request);
            }
        }

        requestQueue.removeAll(unfulfillable);
        return unfulfillable;
    }

    private boolean requestMayBeFulfilled(RecipeAssignmentRequest request)
    {
        eventManager.publish(new RecipeStatusEvent(this, request.getRequest().getId(), "Checking recipe agent requirements..."));
        for (Agent a : agentManager.getOnlineAgents())
        {
            if (request.getHostRequirements().fulfilledBy(request, a.getService()))
            {
                eventManager.publish(new RecipeStatusEvent(this, request.getRequest().getId(), "Requirements satisfied by at least one online agent."));
                return true;
            }
        }

        eventManager.publish(new RecipeStatusEvent(this, request.getRequest().getId(), "No online agents satisfy requirements."));
        return false;
    }

    public void run()
    {
        isRunning = true;
        stopRequested = false;
        LOG.debug("started.");

        // wait for changes to either of the inbound queues. When change detected,
        // copy the new data into the internal queue (to minimize locked time) and
        // start processing.  JS: extended lock time to simplify snapshotting:
        // review iff this leads to a performance issue (seems unlikely).

        while (!stopRequested)
        {
            lock.lock();
            LOG.debug("lock.lock();");
            try
            {
                if (stopRequested)
                {
                    break;
                }

                List<RecipeAssignmentRequest> doneRequests = new LinkedList<RecipeAssignmentRequest>();
                long currentTime = System.currentTimeMillis();

                for (RecipeAssignmentRequest request : requestQueue)
                {
                    if(request.hasTimedOut(currentTime))
                    {
                        doneRequests.add(request);
                        eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), "Recipe request timed out waiting for a capable agent to become available"));
                    }
                    else
                    {
                        BuildRevision buildRevision = request.getRevision();
                        buildRevision.lock();
                        try
                        {
                            Iterable<Agent> agentList = agentSorter.sort(agentManager.getAvailableAgents(), request);
                            for (Agent agent : agentList)
                            {
                                AgentService service = agent.getService();

                                // can the request be sent to this service?
                                if (request.getHostRequirements().fulfilledBy(request, service))
                                {
                                    buildRevision.fix();
                                    eventManager.publish(new RecipeAssignedEvent(this, request.getRequest(), agent));
                                    doneRequests.add(request);
                                    break;
                                }
                            }
                        }
                        finally
                        {
                            buildRevision.unlock();
                        }
                    }
                }

                requestQueue.removeAll(doneRequests);

                try
                {
                    // Wake up when there is something to do, and also
                    // periodically to check for timed-out requests.
                    LOG.debug("lockCondition.await();");
                    lockCondition.await(sleepInterval, TimeUnit.MILLISECONDS);
                    LOG.debug("lockCondition.unawait();");
                }
                catch (InterruptedException e)
                {
                    LOG.debug("lockCondition.wait() was interrupted: " + e.getMessage());
                }
            }
            finally
            {
                lock.unlock();
                LOG.debug("lock.unlock();");
            }
        }

        executor.shutdown();
        LOG.debug("stopped.");
        isRunning = false;
    }

    public void stop(boolean force)
    {
        if (isStopped())
        {
            throw new IllegalStateException("The queue is already stopped.");
        }

        lock.lock();
        try
        {
            LOG.debug("stop();");
            stopRequested = true;
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean isStopped()
    {
        return !isRunning();
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public int length()
    {
        lock.lock();
        try
        {
            return requestQueue.size();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof AgentAvailableEvent)
        {
            handleAvaialbleEvent();
        }
        else if (evt instanceof AgentConnectivityEvent)
        {
            handleConnectivityEvent((AgentConnectivityEvent) evt);
        }
        else if (evt instanceof BuildRevisionUpdatedEvent)
        {
            handleBuildRevisionUpdated((BuildRevisionUpdatedEvent) evt);
        }
        else if (evt instanceof AgentResourcesDiscoveredEvent)
        {
            updateTimeoutsForAgent(((AgentResourcesDiscoveredEvent) evt).getAgent());
        }
        else if (evt instanceof ConfigurationEventSystemStartedEvent)
        {
            ConfigurationProvider configurationProvider = ((ConfigurationEventSystemStartedEvent)evt).getConfigurationProvider();
            globalConfiguration = configurationProvider.get(GlobalConfiguration.class);
            updateTimeout(globalConfiguration);
            configurationProvider.registerEventListener(this, false, false, GlobalConfiguration.class);
        }
        else if (evt instanceof SystemStartedEvent)
        {
            init();
        }
    }

    private void handleAvaialbleEvent()
    {
        lock.lock();
        try
        {
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    private void handleConnectivityEvent(AgentConnectivityEvent event)
    {
        if (event instanceof AgentOnlineEvent)
        {
            updateTimeoutsForAgent(event.getAgent());
        }
        else
        {
            offline();
        }
    }

    private void handleBuildRevisionUpdated(BuildRevisionUpdatedEvent event)
    {
        List<RecipeAssignmentRequest> rejects = null;
        lock.lock();
        try
        {
            List<RecipeAssignmentRequest> unfulfillable = checkQueueForChanges(event, requestQueue);
            if(unsatisfiableTimeout == 0)
            {
                requestQueue.removeAll(unfulfillable);
                rejects = unfulfillable;
            }
            else if(unsatisfiableTimeout > 0)
            {
                updateTimeouts(unfulfillable, System.currentTimeMillis() + unsatisfiableTimeout);
            }
        }
        finally
        {
            lock.unlock();
        }

        // Publish events outside the lock
        if(rejects != null)
        {
            publishUnfulfillable(rejects);
        }
    }

    private void updateTimeouts(List<RecipeAssignmentRequest> requests, long timeout)
    {
        for(RecipeAssignmentRequest request: requests)
        {
            if(!request.hasTimeout())
            {
                request.setTimeout(timeout);
            }
        }
    }

    private void publishUnfulfillable(List<RecipeAssignmentRequest> unfulfillable)
    {
        for (RecipeAssignmentRequest request : unfulfillable)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), "No online agent is capable of executing the build stage"));
        }
    }

    private List<RecipeAssignmentRequest> checkQueueForChanges(BuildRevisionUpdatedEvent event, RequestQueue requests)
    {
        List<RecipeAssignmentRequest> unfulfillable = new LinkedList<RecipeAssignmentRequest>();
        for (RecipeAssignmentRequest request : requests)
        {
            if (request.getBuild().getId() == event.getBuildResult().getId())
            {
                try
                {
                    if (!requestMayBeFulfilled(request))
                    {
                        unfulfillable.add(request);
                    }
                }
                catch (Exception e)
                {
                    // We already have a revision, so this is not fatal.
                    LOG.warning("Unable to check build revision: " + e.getMessage(), e);
                }
            }
        }

        return unfulfillable;
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{
                AgentAvailableEvent.class,
                AgentConnectivityEvent.class,
                AgentResourcesDiscoveredEvent.class,
                BuildRevisionUpdatedEvent.class,
                ConfigurationEventSystemStartedEvent.class,
                SystemStartedEvent.class
        };
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if(event instanceof PostSaveEvent)
        {
            globalConfiguration = (GlobalConfiguration) event.getInstance();
            updateTimeout(globalConfiguration);
        }
    }

    private void updateTimeout(GlobalConfiguration globalConfiguration)
    {
        long timeout = globalConfiguration.getRecipeTimeout();
        if(timeout > 0)
        {
            timeout *= Constants.MINUTE;
        }

        this.unsatisfiableTimeout = timeout;
    }

    public void setUnsatisfiableTimeout(int milliseconds)
    {
        this.unsatisfiableTimeout = milliseconds;
    }

    public void setSleepInterval(long milliseconds)
    {
        this.sleepInterval = milliseconds;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(this);
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setAgentSorter(AgentSorter agentSorter)
    {
        this.agentSorter = agentSorter;
    }

    /**
     * Allow easy / safe access to a snapshot of the list of recipe dispatch requests.  Changes
     * to the list itself are synchronized so that the snapshot is not taken in the middle of a
     * change.  However, and importantly, the snapshot is not bound to the synchronization
     * taking place within the ThreadedRecipeQueue.
     *
     * See CIB-1401.
     */
    private class RequestQueue implements Iterable<RecipeAssignmentRequest>
    {
        private final List<RecipeAssignmentRequest> list = new LinkedList<RecipeAssignmentRequest>();

        public synchronized void add(RecipeAssignmentRequest item)
        {
            list.add(item);
        }

        public synchronized void remove(RecipeAssignmentRequest item)
        {
            list.remove(item);
        }

        public synchronized void addAll(Collection<RecipeAssignmentRequest> items)
        {
            list.addAll(items);
        }

        public synchronized void removeAll(Collection<RecipeAssignmentRequest> items)
        {
            list.removeAll(items);
        }

        public synchronized List<RecipeAssignmentRequest> snapshot()
        {
            return new LinkedList<RecipeAssignmentRequest>(list);
        }

        public Iterator<RecipeAssignmentRequest> iterator()
        {
            return list.iterator();
        }

        public synchronized int size()
        {
            return list.size();
        }
    }
}
