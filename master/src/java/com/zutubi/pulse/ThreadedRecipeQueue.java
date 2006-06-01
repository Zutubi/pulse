/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.events.build.RecipeEvent;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.net.MalformedURLException;

/**
 * <class-comment/>
 */
public class ThreadedRecipeQueue implements Runnable, RecipeQueue, EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(ThreadedRecipeQueue.class);

    private boolean checkOnEnqueue = true;

    private ObjectFactory objectFactory;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition lockCondition = lock.newCondition();

    private final Map<Long, Agent> onlineAgents = new TreeMap<Long, Agent>();

    /**
     * The queue to which new dispatch requests are added.
     */
    private final List<RecipeDispatchRequest> newDispatches = new LinkedList<RecipeDispatchRequest>();

    /**
     * The internal queue of dispatch requests.
     */
    private final List<RecipeDispatchRequest> queuedDispatches = new LinkedList<RecipeDispatchRequest>();

    private final List<Agent> newAgents = new LinkedList<Agent>();
    private final Map<Long, Agent> availableAgents = new TreeMap<Long, Agent>();

    /**
     * Maps from recipe ID to the agent executing the recipe.
     */
    private final Map<Long, Agent> executingAgents = new TreeMap<Long, Agent>();

    private ExecutorService executor;

    private boolean stopRequested = false;

    private boolean isRunning = false;

    private AgentManager agentManager;
    private EventManager eventManager;

    private SlaveProxyFactory slaveProxyFactory;

    public ThreadedRecipeQueue()
    {

    }

    public void init()
    {
        try
        {
            // Get all agents
            for(Agent a: agentManager.getOnlineAgents())
            {
                available(a);
            }

            eventManager.register(this);
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
        executor = Executors.newSingleThreadExecutor();
        executor.execute(this);
    }

    public void stop()
    {
        stop(true);
    }

    /**
     * Enqueue a new recipe dispatch request.
     *
     * @param dispatchRequest
     */
    public void enqueue(RecipeDispatchRequest dispatchRequest)
    {
        lock.lock();
        try
        {
            if(requestMayBeFulfilled(dispatchRequest))
            {
                newDispatches.add(dispatchRequest);
                dispatchRequest.queued();
                lockCondition.signal();
            }
            else
            {
                eventManager.publish(new RecipeErrorEvent(this, dispatchRequest.getRequest().getId(), "No online agent is capable of executing the build stage"));
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public List<RecipeDispatchRequest> takeSnapshot()
    {
        List<RecipeDispatchRequest> snapshot = new LinkedList<RecipeDispatchRequest>();

        lock.lock();
        try
        {
            snapshot.addAll(queuedDispatches);
            snapshot.addAll(newDispatches);
        }
        finally
        {
            lock.unlock();
        }

        return snapshot;
    }

    public boolean cancelRequest(long id)
    {
        boolean removed = false;

        try
        {
            lock.lock();
            RecipeDispatchRequest removeRequest = null;

            for (RecipeDispatchRequest request : newDispatches)
            {
                if (request.getRequest().getId() == id)
                {
                    removeRequest = request;
                    break;
                }
            }

            if (removeRequest != null)
            {
                newDispatches.remove(removeRequest);
                removed = true;
            }
            else
            {
                for (RecipeDispatchRequest request : queuedDispatches)
                {
                    if (request.getRequest().getId() == id)
                    {
                        removeRequest = request;
                        break;
                    }
                }

                if (removeRequest != null)
                {
                    queuedDispatches.remove(removeRequest);
                    removed = true;
                }
            }
        }
        finally
        {
            lock.unlock();
        }

        return removed;
    }

    void available(Agent agent)
    {
        lock.lock();
        try
        {
            onlineAgents.put(agent.getId(), agent);
            newAgents.add(agent);
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    private void unavailable(Agent agent)
    {
        lock.lock();
        try
        {
            onlineAgents.remove(agent.getId());

            // Check all queued requests may still be handled by an online
            // agent.
            checkQueuedRequests();

            long deadRecipe = 0;
            for(Map.Entry<Long, Agent> entry: executingAgents.entrySet())
            {
                if(entry.getValue().getId() == agent.getId())
                {
                    // Agent dropped off while we were executing.
                    deadRecipe = entry.getKey();
                    break;
                }
            }

            if(deadRecipe != 0)
            {
                // Remove it first so we don't find it when handling this event.
                executingAgents.remove(deadRecipe);
                eventManager.publish(new RecipeErrorEvent(this, deadRecipe, "Connection to agent lost during recipe execution"));
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void checkQueuedRequests()
    {
        List<RecipeDispatchRequest> unfulfillable = new LinkedList<RecipeDispatchRequest>();
        for(RecipeDispatchRequest request: queuedDispatches)
        {
            if(!requestMayBeFulfilled(request))
            {
                unfulfillable.add(request);
            }
        }

        for(RecipeDispatchRequest request: unfulfillable)
        {
            queuedDispatches.remove(request);
            eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), "No online agent is capable of executing the build stage"));
        }
    }

    private boolean requestMayBeFulfilled(RecipeDispatchRequest request)
    {
        if(!checkOnEnqueue)
        {
            return true;
        }

        for(Agent a: onlineAgents.values())
        {
            if(request.getHostRequirements().fulfilledBy(a.getBuildService()))
            {
                return true;
            }
        }

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
                if (newDispatches.size() == 0 && newAgents.size() == 0)
                {
                    try
                    {
                        LOG.debug("lockCondition.await();");
                        lockCondition.await(60, TimeUnit.SECONDS);
                        LOG.debug("lockCondition.unawait();");
                    }
                    catch (InterruptedException e)
                    {
                        LOG.debug("lockCondition.wait() was interrupted: " + e.getMessage());
                    }
                }

                if (stopRequested)
                {
                    break;
                }

                queuedDispatches.addAll(newDispatches);
                newDispatches.clear();

                for(Agent a: newAgents)
                {
                    availableAgents.put(a.getId(), a);
                }
                newAgents.clear();

                List<RecipeDispatchRequest> dispatchedRequests = new LinkedList<RecipeDispatchRequest>();
                List<Agent> unavailableAgents = new LinkedList<Agent>();

                for (RecipeDispatchRequest request : queuedDispatches)
                {
                    for (Agent agent: availableAgents.values())
                    {
                        BuildService service = agent.getBuildService();

                        // can the request be sent to this service?
                        if (request.getHostRequirements().fulfilledBy(service) && !unavailableAgents.contains(agent))
                        {
                            if(dispatchRequest(request, agent, unavailableAgents, dispatchedRequests))
                            {
                                break;
                            }
                        }
                    }
                }

                queuedDispatches.removeAll(dispatchedRequests);

                for(Agent a: unavailableAgents)
                {
                    availableAgents.remove(a.getId());
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

    private boolean dispatchRequest(RecipeDispatchRequest request, Agent agent, List<Agent> unavailableAgents, List<RecipeDispatchRequest> dispatchedRequests)
    {
        try
        {
            request.prepare();
        }
        catch (Exception e)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), "Error dispatching recipe: " + e.getMessage()));

            // We consider this dispatched in the sense that it should be dequeued
            dispatchedRequests.add(request);
            return true;
        }

        if(agent.getBuildService().build(request.getRequest()))
        {
            dispatchedRequests.add(request);
            unavailableAgents.add(agent);
            lock.lock();
            try
            {
                executingAgents.put(request.getRequest().getId(), agent);
            }
            finally
            {
                lock.unlock();
            }

            eventManager.publish(new RecipeDispatchedEvent(this, request.getRequest(), agent));
            return true;
        }
        else
        {
            return false;
        }
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
            return queuedDispatches.size() + newDispatches.size();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void handleEvent(Event evt)
    {
        if(evt instanceof SlaveEvent)
        {
            handleSlaveEvent((SlaveEvent)evt);
        }
        else if(evt instanceof RecipeEvent)
        {
            handleRecipeEvent((RecipeEvent) evt);
        }
    }

    private void handleSlaveEvent(SlaveEvent event)
    {
        Agent a = agentManager.getAgent(event.getSlave());
        if(event instanceof SlaveAvailableEvent)
        {
            available(a);
        }
        else
        {
            unavailable(a);
        }
    }

    private void handleRecipeEvent(RecipeEvent event)
    {
        lock.lock();
        try
        {
            Agent agent = executingAgents.get(event.getRecipeId());

            // The agent could be null if there was a loss of communication
            // with the agent leading to abortion of the recipe on the master.
            // In that case, the agent is not now available.
            if (agent != null)
            {
                executingAgents.remove(event.getRecipeId());
                available(agent);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setCheckOnEnqueue(boolean checkOnEnqueue)
    {
        this.checkOnEnqueue = checkOnEnqueue;
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeCompletedEvent.class, RecipeErrorEvent.class, SlaveEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setSlaveProxyFactory(SlaveProxyFactory slaveProxyFactory)
    {
        this.slaveProxyFactory = slaveProxyFactory;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
