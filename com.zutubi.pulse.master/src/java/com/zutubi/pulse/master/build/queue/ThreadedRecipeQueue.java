package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.events.RecipeStatusEvent;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.AgentSorter;
import com.zutubi.pulse.master.agent.DefaultAgentSorter;
import com.zutubi.pulse.master.events.AgentAvailableEvent;
import com.zutubi.pulse.master.events.AgentConnectivityEvent;
import com.zutubi.pulse.master.events.AgentOnlineEvent;
import com.zutubi.pulse.master.events.AgentResourcesDiscoveredEvent;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.config.events.PostSaveEvent;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.util.Constants;
import com.zutubi.util.RetryHandler;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.time.Clock;
import com.zutubi.util.time.SystemClock;

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

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.removeIf;

/**
 * A recipe queue that runs an independent thread to manage the dispatching
 * of recipes.
 */
public class ThreadedRecipeQueue implements Runnable, RecipeQueue, EventListener, Stoppable, ConfigurationEventListener
{
    private static final Messages I18N = Messages.getInstance(ThreadedRecipeQueue.class);
    private static final Logger LOG = Logger.getLogger(ThreadedRecipeQueue.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition lockCondition = lock.newCondition();
    
    private Clock clock = new SystemClock();

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
    private ThreadFactory threadFactory;
    private AgentSorter agentSorter = new DefaultAgentSorter();

    public void init()
    {
        try
        {
            // Get all agents
            for (Agent a : agentManager.getOnlineAgents())
            {
                resetTimeoutsForAgent(a);
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
        try
        {
            lock.lock();

            if (isRunning())
            {
                throw new IllegalStateException(I18N.format("illegal.state.running"));
            }

            LOG.info("Stage queue resumed by '" + SecurityUtils.getLoggedInUsername() + "'");
            isRunning = true;
            executor = Executors.newSingleThreadExecutor(threadFactory);
            executor.execute(this);
            stopRequested = false;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void stop()
    {
        LOG.info("Stage queue paused by '" + SecurityUtils.getLoggedInUsername() + "'");
        stop(true);
    }

    /**
     * Enqueue a new recipe dispatch request.
     *
     * @param assignmentRequest the request to be enqueued
     */
    public void enqueue(RecipeAssignmentRequest assignmentRequest)
    {
        LOG.debug("enqueue(" + assignmentRequest.getRequest().getId() + "): started");
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
                    if (unsatisfiableTimeout == 0)
                    {
                        error = new RecipeErrorEvent(this, assignmentRequest.getRequest().getId(), I18N.format("satisfy.requirements.none", assignmentRequest.getUnfulfilledReason()), false);
                    }
                    else
                    {
                        if (unsatisfiableTimeout > 0)
                        {
                            assignmentRequest.setTimeout(clock.getCurrentTimeMillis() + unsatisfiableTimeout);
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
            error = new RecipeErrorEvent(this, assignmentRequest.getRequest().getId(), I18N.format("error.enqueue.failed", e.getMessage()), false);
        }

        if (error != null)
        {
            // Publish outside the lock.
            eventManager.publish(error);
        }

        LOG.debug("enqueue(" + assignmentRequest.getRequest().getId() + "): done");
    }

    private void addToQueue(RecipeAssignmentRequest assignmentRequest)
    {
        LOG.debug("Adding request " + assignmentRequest.getRequest().getId() + " to the queue");
        requestQueue.add(assignmentRequest);
        assignmentRequest.queued(clock.getCurrentTimeMillis());
        lockCondition.signal();
    }

    public List<RecipeAssignmentRequest> takeSnapshot()
    {
        return requestQueue.snapshot();
    }

    /**
     * Cancelled the queued recipe request.
     *
     * @param recipeId the unique identifier for the recipe to be cancelled.
     * @return true if the recipe was found in the queue and cancelled, false
     *         otherwise.
     */
    public boolean cancelRequest(final long recipeId)
    {
        LOG.debug("cancelRequest(" + recipeId + "): started");
        boolean removed = false;

        try
        {
            lock.lock();
            RecipeAssignmentRequest removeRequest = find(requestQueue, new Predicate<RecipeAssignmentRequest>()
            {
                public boolean apply(RecipeAssignmentRequest request)
                {
                    return request.getRequest().getId() == recipeId;
                }
            }, null);

            if (removeRequest != null)
            {
                LOG.debug("Removing request " + removeRequest.getRequest().getId() + " from the queue");
                requestQueue.remove(removeRequest);
                removed = true;
            }
        }
        finally
        {
            lock.unlock();
        }

        LOG.debug("cancelRequest(" + recipeId + "): done");
        return removed;
    }

    /**
     * The specified agent is now online, so reset the recipe assignment
     * request unsatisfiable timeout for any requests that can be satisfied
     * by this agent.
     *
     * @param agent the agent that is available for builds.
     */
    private void resetTimeoutsForAgent(Agent agent)
    {
        LOG.debug("resetTimeoutsForAgent(" + agent.getName() + "): started");
        lock.lock();
        try
        {
            for (RecipeAssignmentRequest request : requestQueue)
            {
                if (request.hasTimeout() && request.isFulfilledBy(agent))
                {
                    request.clearTimeout();
                }
            }
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
        LOG.debug("resetTimeoutsForAgent(" + agent.getName() + "): done");
    }

    /**
     * An agent has gone offline, so check the unsatisfiable timeout, updating
     * timeouts or removing requests as necessary.
     */
    void offline()
    {
        LOG.debug("offline(): started");
        List<RecipeAssignmentRequest> removedRequests = null;

        lock.lock();
        try
        {

            if (unsatisfiableTimeout == 0)
            {
                removedRequests = removeUnfulfillable();
            }
            else if (unsatisfiableTimeout > 0)
            {
                checkQueuedTimeouts(clock.getCurrentTimeMillis() + unsatisfiableTimeout);
            }

            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }

        // Publish the events outside of the locking.
        if (removedRequests != null)
        {
            publishUnfulfillable(removedRequests);
        }
        LOG.debug("offline(): done");
    }

    private void checkQueuedTimeouts(long timeout)
    {
        assert (lock.isHeldByCurrentThread());

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
        assert (lock.isHeldByCurrentThread());

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
        eventManager.publish(new RecipeStatusEvent(this, request.getRequest().getId(), I18N.format("satisfy.requirements.check")));
        for (Agent agent : agentManager.getOnlineAgents())
        {
            if (request.isFulfilledBy(agent))
            {
                eventManager.publish(new RecipeStatusEvent(this, request.getRequest().getId(), I18N.format("satisfy.requirements.some")));
                return true;
            }
        }

        eventManager.publish(new RecipeStatusEvent(this, request.getRequest().getId(), I18N.format("satisfy.requirements.none", request.getUnfulfilledReason())));
        return false;
    }

    public void run()
    {
        RetryHandler retryHandler = new RetryHandler(1, TimeUnit.SECONDS, 1800, TimeUnit.SECONDS);
        retryHandler.setBackoff(true);
        retryHandler.setExponentialBackoff(true);

        try
        {
            lock.lock();
            while (!stopRequested)
            {
                doAssignmentIteration(retryHandler);
            }

            executor.shutdown();
        }
        finally
        {
            isRunning = false;
            lock.unlock();
        }
    }

    private void doAssignmentIteration(RetryHandler retryHandler)
    {
        try
        {
            LOG.finer("Begin assignment loop");
            final List<RecipeAssignmentRequest> doneRequests = new LinkedList<RecipeAssignmentRequest>();
            long currentTime = clock.getCurrentTimeMillis();

            // Notes on the agent pool:
            // With the introduction of priority ordering to the request queue, we need provide
            // better control over the agents available for recipe assignment.  In short, the
            // set of agents available to any particular recipe request needs to be a subset of
            // what was available to earlier requests.  Otherwise, an agent becoming available
            // half way through the processing of the request queue can 'activate' a low priority
            // request when it should have been used for a higher priority request.
            final List<Agent> agentPool = new LinkedList<Agent>();
            agentManager.withAvailableAgents(new UnaryProcedure<List<Agent>>()
            {
                public void run(List<Agent> agents)
                {
                    agentPool.addAll(agents);
                }
            });

            LOG.finest("  agent pool acquired");
            for (final RecipeAssignmentRequest request : requestQueue)
            {
                final long recipeId = request.getRequest().getId();
                if (request.hasTimedOut(currentTime))
                {
                    LOG.finest("  request " + recipeId + " timed out");
                    doneRequests.add(request);
                    eventManager.publish(new RecipeErrorEvent(this, recipeId, I18N.format("recipe.assignment.timeout"), false));
                }
                else
                {
                    LOG.finest("  trying to dispatch request " + recipeId);
                    agentManager.withAvailableAgents(new UnaryProcedure<List<Agent>>()
                    {
                        // Note that this method must be fast - we are locking agents.
                        // The lock is require to prevent the agent state changing
                        // before we assign the recipe to it.
                        public void run(final List<Agent> availableAgents)
                        {
                            // The agent pool can only contain agents that are currently available.
                            removeIf(agentPool, not(in(availableAgents)));

                            Iterable<Agent> agentList = agentSorter.sort(agentPool, request);
                            for (Agent agent : agentList)
                            {
                                // can the request be sent to this service?
                                if (request.isFulfilledBy(agent))
                                {
                                    LOG.finest("  dispatching request " + recipeId + " to agent '" + agent.getName() + "'");
                                    eventManager.publish(new RecipeAssignedEvent(this, request.getRequest(), agent));
                                    doneRequests.add(request);
                                    break;
                                }
                            }
                        }
                    });
                }
            }

            LOG.finer("End assignment loop (dispatched " + doneRequests.size() + " requests)");
            requestQueue.removeAll(doneRequests);

            try
            {
                // Wake up when there is something to do, and also
                // periodically to check for timed-out requests.
                lockCondition.await(sleepInterval, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                LOG.debug("lockCondition.wait() was interrupted: " + e.getMessage());
            }

            retryHandler.reset();
        }
        catch (Throwable t)
        {
            retryHandler.handle(t);
        }
    }

    public void stop(boolean force)
    {
        lock.lock();
        try
        {
            stopRequested = true;
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Returns true if the recipe queue is not running.
     *
     * @return true if the queue is not running.
     * @see #isRunning()
     */
    public boolean isStopped()
    {
        return !isRunning();
    }

    public boolean isRunning()
    {
        lock.lock();
        try
        {
            return isRunning;
        }
        finally
        {
            lock.unlock();
        }
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
            handleAvailableEvent();
        }
        else if (evt instanceof AgentConnectivityEvent)
        {
            handleConnectivityEvent((AgentConnectivityEvent) evt);
        }
        else if (evt instanceof AgentResourcesDiscoveredEvent)
        {
            resetTimeoutsForAgent(((AgentResourcesDiscoveredEvent) evt).getAgent());
        }
        else if (evt instanceof ConfigurationEventSystemStartedEvent)
        {
            ConfigurationProvider configurationProvider = ((ConfigurationEventSystemStartedEvent) evt).getConfigurationProvider();
            configurationProvider.registerEventListener(this, false, false, GlobalConfiguration.class);
            updateTimeout(configurationProvider.get(GlobalConfiguration.class));
        }
        else if (evt instanceof SystemStartedEvent)
        {
            init();
        }
    }

    private void handleAvailableEvent()
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
            resetTimeoutsForAgent(event.getAgent());
        }
        else
        {
            offline();
        }
    }

    private void publishUnfulfillable(List<RecipeAssignmentRequest> unfulfillable)
    {
        for (RecipeAssignmentRequest request : unfulfillable)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), I18N.format("satisfy.requirements.none", request.getUnfulfilledReason()), false));
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{
                AgentAvailableEvent.class,
                AgentConnectivityEvent.class,
                AgentResourcesDiscoveredEvent.class,
                ConfigurationEventSystemStartedEvent.class,
                SystemStartedEvent.class
        };
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if (event instanceof PostSaveEvent)
        {
            updateTimeout((GlobalConfiguration) event.getInstance());
        }
    }

    private void updateTimeout(GlobalConfiguration globalConfiguration)
    {
        this.unsatisfiableTimeout = globalConfiguration.getRecipeTimeout() * Constants.MINUTE;
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

    public void setClock(Clock clock)
    {
        this.clock = clock;
    }

    /**
     * Allow easy / safe access to a snapshot of the list of recipe dispatch requests.  Changes
     * to the list itself are synchronised so that the snapshot is not taken in the middle of a
     * change.  However, and importantly, the snapshot is not bound to the synchronisation
     * taking place within the ThreadedRecipeQueue.
     * <p/>
     * See CIB-1401.
     */
    private class RequestQueue implements Iterable<RecipeAssignmentRequest>
    {
        private final LinkedList<RecipeAssignmentRequest> list = new LinkedList<RecipeAssignmentRequest>();

        public synchronized void add(final RecipeAssignmentRequest item)
        {
            RecipeAssignmentRequest request = find(list, new Predicate<RecipeAssignmentRequest>()
            {
                public boolean apply(RecipeAssignmentRequest r)
                {
                    return r.getPriority() < item.getPriority();
                }
            }, null);
            if (request != null)
            {
                list.add(list.indexOf(request), item);
            }
            else
            {
                list.add(item);
            }
        }

        public synchronized void remove(RecipeAssignmentRequest item)
        {
            list.remove(item);
        }

        public synchronized void addAll(Collection<RecipeAssignmentRequest> items)
        {
            for (RecipeAssignmentRequest item : items)
            {
                add(item);
            }
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
