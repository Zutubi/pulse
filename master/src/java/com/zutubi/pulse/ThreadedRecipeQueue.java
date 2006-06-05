package com.zutubi.pulse;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.events.build.RecipeEvent;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMChangeEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <class-comment/>
 */
public class ThreadedRecipeQueue implements Runnable, RecipeQueue, EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(ThreadedRecipeQueue.class);

    private boolean checkOnEnqueue = true;

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
        RecipeErrorEvent error = null;

        lock.lock();
        try
        {
            try
            {
                determineRevision(dispatchRequest);

                if(requestMayBeFulfilled(dispatchRequest))
                {
                    newDispatches.add(dispatchRequest);
                    dispatchRequest.queued();
                    lockCondition.signal();
                }
                else
                {
                    error = new RecipeErrorEvent(this, dispatchRequest.getRequest().getId(), "No online agent is capable of executing the build stage");
                }
            }
            catch (Exception e)
            {
                error = new RecipeErrorEvent(this, dispatchRequest.getRequest().getId(), "Unable to determine revision to build: " + e.getMessage());
            }
        }
        finally
        {
            lock.unlock();
        }

        if(error != null)
        {
            // Publish outside the lock.
            eventManager.publish(error);
        }
    }

    private void determineRevision(RecipeDispatchRequest dispatchRequest) throws BuildException, SCMException
    {
        BuildRevision buildRevision = dispatchRequest.getRevision();
        if(!buildRevision.isInitialised())
        {
            // Let's initialise it
            Project project = dispatchRequest.getBuild().getProject();
            Scm scm = project.getScm();
            Revision revision = scm.createServer().getLatestRevision();

            // May throw a BuildException
            updateRevision(dispatchRequest, revision);
        }
    }

    private void updateRevision(RecipeDispatchRequest dispatchRequest, Revision revision) throws BuildException
    {
        Project project = dispatchRequest.getBuild().getProject();
        String pulseFile = project.getPulseFileDetails().getPulseFile(dispatchRequest.getRequest().getId(), project, revision);
        dispatchRequest.getRevision().update(revision, pulseFile);
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
        RecipeErrorEvent error = null;
        List<RecipeDispatchRequest> unfulfillable = new LinkedList<RecipeDispatchRequest>();

        lock.lock();
        try
        {
            onlineAgents.remove(agent.getId());

            // Check all queued requests may still be handled by an online
            // agent.
            unfulfillable.addAll(checkQueuedRequests(queuedDispatches));
            unfulfillable.addAll(checkQueuedRequests(newDispatches));

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
                error = new RecipeErrorEvent(this, deadRecipe, "Connection to agent lost during recipe execution");
            }
        }
        finally
        {
            lock.unlock();
        }

        if(error != null)
        {
            // Publish outside the lock.
            eventManager.publish(error);
        }

        publishUnfulfillable(unfulfillable);
    }

    private List<RecipeDispatchRequest> checkQueuedRequests(List<RecipeDispatchRequest> requests)
    {
        assert(lock.isHeldByCurrentThread());

        List<RecipeDispatchRequest> unfulfillable = new LinkedList<RecipeDispatchRequest>();
        for(RecipeDispatchRequest request: requests)
        {
            if(!requestMayBeFulfilled(request))
            {
                unfulfillable.add(request);
            }
        }

        removeUnfulfillable(unfulfillable, requests);
        return unfulfillable;
    }

    private boolean requestMayBeFulfilled(RecipeDispatchRequest request)
    {
        if(!checkOnEnqueue)
        {
            return true;
        }

        for(Agent a: onlineAgents.values())
        {
            if(request.getHostRequirements().fulfilledBy(request, a.getBuildService()))
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
                        if (request.getHostRequirements().fulfilledBy(request, service) && !unavailableAgents.contains(agent))
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
        request.getRevision().apply(request.getRequest());

        if(agent.getBuildService().build(request.getRequest()))
        {
            dispatchedRequests.add(request);
            unavailableAgents.add(agent);

            // We can no longer update the revision once we have dispatched a request.
            request.getRevision().fix();
            executingAgents.put(request.getRequest().getId(), agent);

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

    public int executingCount()
    {
        lock.lock();
        try
        {
            return executingAgents.size();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void handleEvent(Event evt)
    {
        if(evt instanceof RecipeEvent)
        {
            handleRecipeEvent((RecipeEvent) evt);
        }
        else if(evt instanceof SlaveEvent)
        {
            handleSlaveEvent((SlaveEvent)evt);
        }
        else if(evt instanceof SCMChangeEvent)
        {
            handleScmChange((SCMChangeEvent)evt);
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

    private void handleScmChange(SCMChangeEvent event)
    {
        List<RecipeDispatchRequest> unfulfillable = new LinkedList<RecipeDispatchRequest>();
        Scm changedScm = event.getScm();
        lock.lock();
        try
        {
            unfulfillable.addAll(checkQueueForChanges(changedScm, event, newDispatches));
            unfulfillable.addAll(checkQueueForChanges(changedScm, event, queuedDispatches));
        }
        finally
        {
            lock.unlock();
        }

        // Publish events outside the lock
        publishUnfulfillable(unfulfillable);
    }

    private void publishUnfulfillable(List<RecipeDispatchRequest> unfulfillable)
    {
        for(RecipeDispatchRequest request: unfulfillable)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), "No online agent is capable of executing the build stage"));
        }
    }

    private List<RecipeDispatchRequest> checkQueueForChanges(Scm changedScm, SCMChangeEvent event, List<RecipeDispatchRequest> requests)
    {
        List<RecipeDispatchRequest> unfulfillable = new LinkedList<RecipeDispatchRequest>();

        for(RecipeDispatchRequest request: requests)
        {
            Scm requestScm = request.getBuild().getProject().getScm();
            if(!request.getRevision().isFixed() && requestScm.getId() == changedScm.getId())
            {
                try
                {
                    updateRevision(request, event.getNewRevision());
                    if(!requestMayBeFulfilled(request))
                    {
                        unfulfillable.add(request);
                    }
                }
                catch(Exception e)
                {
                    // We already have a revision, so this is not fatal.
                    LOG.warning("Unable to check build revision: " + e.getMessage(), e);
                }
            }
        }

        removeUnfulfillable(unfulfillable, requests);
        return unfulfillable;
    }

    private void removeUnfulfillable(List<RecipeDispatchRequest> unfulfillable, List<RecipeDispatchRequest> requests)
    {
        for(RecipeDispatchRequest request: unfulfillable)
        {
            requests.remove(request);
        }
    }

    public void setCheckOnEnqueue(boolean checkOnEnqueue)
    {
        this.checkOnEnqueue = checkOnEnqueue;
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeCompletedEvent.class, RecipeErrorEvent.class, SCMChangeEvent.class, SlaveEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
