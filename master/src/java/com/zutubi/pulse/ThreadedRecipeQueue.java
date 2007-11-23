package com.zutubi.pulse;

import com.zutubi.prototype.config.ConfigurationEventListener;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import static com.zutubi.pulse.MasterBuildProperties.*;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.build.*;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.types.TypeConfiguration;
import com.zutubi.pulse.scm.ScmChangeEvent;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
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
     * A map from agent id to agent for all online agents.  Used to determine
     * which requests are fulfillable (requests that cannot be handled by an
     * online agent are rejected).
     */
    private final Map<Long, Agent> onlineAgents = new TreeMap<Long, Agent>();

    /**
     * The internal queue of dispatch requests.
     */
    private final List<RecipeDispatchRequest> queuedDispatches = new LinkedList<RecipeDispatchRequest>();

    /**
     * Map from agent id to agent for all agents that are available to
     * receive recipe requests.  Only touched by the run thread.
     */
    private final Map<Long, Agent> availableAgents = new TreeMap<Long, Agent>();

    /**
     * Maps from recipe ID to the agent executing the recipe for all agents
     * that are currently executing a build for us.
     */
    private final Map<Long, Agent> executingAgents = new TreeMap<Long, Agent>();

    private ExecutorService executor;

    private boolean stopRequested = false;
    private boolean isRunning = false;
    /**
     * * Maximum number of seconds between checks of the queue.  Usually
     * checks occur due to the condition being flagged, but we need to
     * wake up periodically to enforce timeouts.
     */
    private int sleepInterval = 60;
    /**
     * Maximum number of millis to leave a request that cannot be satisfied
     * in the queue.  This is based on how long there has been no capable
     * agent available (not on how long the request has been queued).  If the
     * timeout is 0, unsatisfiable requests will be rejected immediately.  If
     * the timeout is negative, requests will never time out.
     */
    private long unsatisfiableTimeout = 0;

    private BlockingQueue<DispatchedRequest> dispatchedQueue = new LinkedBlockingQueue<DispatchedRequest>();
    
    private AgentManager agentManager;
    private EventManager eventManager;
    private ResourceManager resourceManager;
    private GeneralAdminConfiguration adminConfiguration;
    private ScmClientFactory scmClientFactory;
    private ThreadFactory threadFactory;

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
                online(a);
            }

            eventManager.register(this);

            Thread dispatcherThread = new Thread(new Dispatcher(), "Recipe Dispatcher Service");
            dispatcherThread.setDaemon(true);
            dispatcherThread.start();
            
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
     * @param dispatchRequest the request to be enqueued
     */
    public void enqueue(RecipeDispatchRequest dispatchRequest)
    {
        RecipeErrorEvent error = null;

        try
        {
            determineRevision(dispatchRequest);
            
            lock.lock();
            try
            {
                if (requestMayBeFulfilled(dispatchRequest))
                {
                    addToQueue(dispatchRequest);
                }
                else
                {
                    if(unsatisfiableTimeout == 0)
                    {
                        error = new RecipeErrorEvent(this, dispatchRequest.getRequest().getId(), "No online agent is capable of executing the build stage");
                    }
                    else
                    {
                        if(unsatisfiableTimeout > 0)
                        {
                            dispatchRequest.setTimeout(System.currentTimeMillis() + unsatisfiableTimeout);
                        }

                        addToQueue(dispatchRequest);
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
            error = new RecipeErrorEvent(this, dispatchRequest.getRequest().getId(), "Unable to determine revision to build: " + e.getMessage());
        }


        if (error != null)
        {
            // Publish outside the lock.
            eventManager.publish(error);
        }
    }

    private void addToQueue(RecipeDispatchRequest dispatchRequest)
    {
        queuedDispatches.add(dispatchRequest);
        dispatchRequest.queued();
        lockCondition.signal();
    }

    private void determineRevision(RecipeDispatchRequest dispatchRequest) throws BuildException, ScmException
    {
        BuildRevision buildRevision = dispatchRequest.getRevision();
        if (!buildRevision.isInitialised())
        {
            // Let's initialise it
            eventManager.publish(new RecipeStatusEvent(this, dispatchRequest.getRequest().getId(), "Initialising build revision..."));
            ProjectConfiguration projectConfig = dispatchRequest.getProject().getConfig();
            ScmConfiguration scm = projectConfig.getScm();

            ScmClient client = null;
            try
            {
                client = scmClientFactory.createClient(scm);
                Revision revision = client.getLatestRevision();

                // May throw a BuildException
                updateRevision(dispatchRequest, revision);
                eventManager.publish(new RecipeStatusEvent(this, dispatchRequest.getRequest().getId(), "Revision initialised to '" + revision.getRevisionString() + "'"));
            }
            finally
            {
                ScmClientUtils.close(client);
            }
        }
    }

    private void updateRevision(RecipeDispatchRequest dispatchRequest, Revision revision) throws BuildException
    {
        ProjectConfiguration projectConfig = dispatchRequest.getProject().getConfig();
        TypeConfiguration type = projectConfig.getType();
        String pulseFile;
        try
        {
            pulseFile = type.getPulseFile(dispatchRequest.getRequest().getId(), projectConfig, revision, null);
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to retrieve pulse file: " + e.getMessage(), e);
        }
        dispatchRequest.getRevision().update(revision, pulseFile);
    }

    public List<RecipeDispatchRequest> takeSnapshot()
    {
        List<RecipeDispatchRequest> snapshot = new LinkedList<RecipeDispatchRequest>();

        lock.lock();
        try
        {
            snapshot.addAll(queuedDispatches);
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
        finally
        {
            lock.unlock();
        }

        return removed;
    }

    void online(Agent agent)
    {
        lock.lock();
        try
        {
            long handle = agent.getConfig().getHandle();
            if(!onlineAgents.containsKey(handle))
            {
                onlineAgents.put(handle, agent);
                availableAgents.put(handle, agent);
                resetTimeouts(agent);
                lockCondition.signal();
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void resetTimeouts(Agent agent)
    {
        for(RecipeDispatchRequest request: queuedDispatches)
        {
            if(request.hasTimeout() && request.getHostRequirements().fulfilledBy(request, agent.getService()))
            {
                request.clearTimeout();
            }
        }
    }

    void offline(Agent agent)
    {
        RecipeErrorEvent error = null;
        List<RecipeDispatchRequest> removedRequests = null;

        lock.lock();
        try
        {
            onlineAgents.remove(agent.getConfig().getHandle());

            if(unsatisfiableTimeout == 0)
            {
                removedRequests = removeUnfulfillable();
            }
            else if(unsatisfiableTimeout > 0)
            {
                checkQueuedTimeouts(System.currentTimeMillis() + unsatisfiableTimeout);
            }

            long deadRecipe = 0;
            for (Map.Entry<Long, Agent> entry : executingAgents.entrySet())
            {
                if (entry.getValue().getConfig().getHandle() == agent.getConfig().getHandle())
                {
                    // Agent dropped off while we were executing.
                    deadRecipe = entry.getKey();
                    break;
                }
            }

            if (deadRecipe != 0)
            {
                // Remove it first so we don't find it when handling this event.
                executingAgents.remove(deadRecipe);
                error = new RecipeErrorEvent(this, deadRecipe, "Connection to agent lost during recipe execution");
            }

            availableAgents.remove(agent.getConfig().getHandle());
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }

        if (error != null)
        {
            // Publish outside the lock.
            eventManager.publish(error);
        }

        if(removedRequests != null)
        {
            publishUnfulfillable(removedRequests);
        }
    }

    private void checkQueuedTimeouts(long timeout)
    {
        assert(lock.isHeldByCurrentThread());

        for (RecipeDispatchRequest request : queuedDispatches)
        {
            if (!request.hasTimeout() && !requestMayBeFulfilled(request))
            {
                request.setTimeout(timeout);
            }
        }
    }

    private List<RecipeDispatchRequest> removeUnfulfillable()
    {
        assert(lock.isHeldByCurrentThread());

        List<RecipeDispatchRequest> unfulfillable = new LinkedList<RecipeDispatchRequest>();
        for (RecipeDispatchRequest request : queuedDispatches)
        {
            if (!requestMayBeFulfilled(request))
            {
                unfulfillable.add(request);
            }
        }

        queuedDispatches.removeAll(unfulfillable);
        return unfulfillable;
    }

    private boolean requestMayBeFulfilled(RecipeDispatchRequest request)
    {
        eventManager.publish(new RecipeStatusEvent(this, request.getRequest().getId(), "Checking recipe agent requirements..."));
        for (Agent a : onlineAgents.values())
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

                List<RecipeDispatchRequest> doneRequests = new LinkedList<RecipeDispatchRequest>();
                List<Agent> unavailableAgents = new LinkedList<Agent>();
                long currentTime = System.currentTimeMillis();

                for (RecipeDispatchRequest request : queuedDispatches)
                {
                    if(request.hasTimedOut(currentTime))
                    {
                        doneRequests.add(request);
                        eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), "Recipe request timed out waiting for a capable agent to become available"));
                    }
                    else
                    {
                        for (Agent agent : availableAgents.values())
                        {
                            AgentService service = agent.getService();

                            // can the request be sent to this service?
                            if (request.getHostRequirements().fulfilledBy(request, service) && !unavailableAgents.contains(agent))
                            {
                                if (dispatchRequest(request, agent, unavailableAgents, doneRequests))
                                {
                                    break;
                                }
                            }
                        }
                    }
                }

                queuedDispatches.removeAll(doneRequests);

                for (Agent a : unavailableAgents)
                {
                    availableAgents.remove(a.getConfig().getHandle());
                }

                try
                {
                    // Wake up when there is something to do, and also
                    // periodically to check for timed-out requests.
                    LOG.debug("lockCondition.await();");
                    lockCondition.await(sleepInterval, TimeUnit.SECONDS);
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

    private boolean dispatchRequest(RecipeDispatchRequest request, Agent agent, List<Agent> unavailableAgents, List<RecipeDispatchRequest> dispatchedRequests)
    {
        BuildRevision buildRevision = request.getRevision();
        RecipeRequest recipeRequest = request.getRequest();

        // This must be called before publishing the event.
        // We can no longer update the revision once we have dispatched a
        // request: it is fixed here if not already.
        buildRevision.apply(recipeRequest);
        recipeRequest.prepare(agent.getConfig().getName());

        // This code cannot handle an agent rejecting the build
        // (the handling was backed outdue to CIB-553 and the fact that
        // agents do not currently reject builds)
        eventManager.publish(new RecipeDispatchedEvent(this, recipeRequest, agent));
        dispatchedRequests.add(request);
        unavailableAgents.add(agent);
        executingAgents.put(recipeRequest.getId(), agent);

        ExecutionContext context = recipeRequest.getContext();
        addRevisionProperties(context, buildRevision);
        addResourceProperties(context, request.getResourceRequirements(), resourceManager.getAgentRepository(agent.getConfig().getHandle()));
        addProjectProperties(context, request.getProject().getConfig());
        
        context.addInternalString(PROPERTY_CLEAN_BUILD, Boolean.toString(request.getProject().isForceCleanForAgent(agent.getId())));

        dispatchedQueue.offer(new DispatchedRequest(recipeRequest, agent));

        return true;
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
            return queuedDispatches.size();
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
        if (evt instanceof RecipeEvent)
        {
            handleRecipeEvent((RecipeEvent) evt);
        }
        else if (evt instanceof AgentEvent)
        {
            handleSlaveEvent((AgentEvent) evt);
        }
        else if (evt instanceof ScmChangeEvent)
        {
            handleScmChange((ScmChangeEvent) evt);
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
                long handle = agent.getConfig().getHandle();
                if(onlineAgents.containsKey(handle))
                {
                    availableAgents.put(handle, agent);
                    lockCondition.signal();
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void handleSlaveEvent(AgentEvent event)
    {
        if (event instanceof AgentStatusEvent)
        {
            handleAgentStatus((AgentStatusEvent) event);
        }
        else if (event instanceof AgentRemovedEvent)
        {
            offline(event.getAgent());
        }
    }

    private void handleAgentStatus(AgentStatusEvent event)
    {
        Agent agent = event.getAgent();
        if (agent.isOnline())
        {
            online(agent);
        }
        else
        {
            offline(agent);
        }
    }

    private void handleScmChange(ScmChangeEvent event)
    {
        List<RecipeDispatchRequest> rejects = null;
        lock.lock();
        try
        {
            List<RecipeDispatchRequest> unfulfillable = checkQueueForChanges(event.getSource(), event, queuedDispatches);
            if(unsatisfiableTimeout == 0)
            {
                queuedDispatches.removeAll(unfulfillable);
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

    private void updateTimeouts(List<RecipeDispatchRequest> requests, long timeout)
    {
        for(RecipeDispatchRequest request: requests)
        {
            if(!request.hasTimeout())
            {
                request.setTimeout(timeout);
            }
        }
    }

    private void publishUnfulfillable(List<RecipeDispatchRequest> unfulfillable)
    {
        for (RecipeDispatchRequest request : unfulfillable)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), "No online agent is capable of executing the build stage"));
        }
    }

    private List<RecipeDispatchRequest> checkQueueForChanges(ProjectConfiguration changedProject, ScmChangeEvent event, List<RecipeDispatchRequest> requests)
    {
        List<RecipeDispatchRequest> unfulfillable = new LinkedList<RecipeDispatchRequest>();

        for (RecipeDispatchRequest request : requests)
        {
            ProjectConfiguration requestProject = request.getProject().getConfig();
            if (!request.getRevision().isFixed() && requestProject.getProjectId() == changedProject.getProjectId())
            {
                try
                {
                    eventManager.publish(new RecipeStatusEvent(this, request.getRequest().getId(), "Change detected while queued, updating build revision to '" + event.getNewRevision() + "'"));
                    updateRevision(request, event.getNewRevision());
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
        return new Class[]{RecipeCompletedEvent.class, RecipeErrorEvent.class, ScmChangeEvent.class, AgentEvent.class};
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if(event instanceof PostSaveEvent)
        {
            adminConfiguration = (GeneralAdminConfiguration) event.getInstance();
            updateTimeout(adminConfiguration);
        }
    }

    private void updateTimeout(GeneralAdminConfiguration adminConfiguration)
    {
        long timeout = adminConfiguration.getRecipeTimeout();
        if(timeout > 0)
        {
            timeout *= Constants.MINUTE;
        }

        this.unsatisfiableTimeout = timeout;
    }

    public void setUnsatisfiableTimeout(int timeout)
    {
        this.unsatisfiableTimeout = timeout;
    }

    public void setSleepInterval(int sleepInterval)
    {
        this.sleepInterval = sleepInterval;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        adminConfiguration = configurationProvider.get(GeneralAdminConfiguration.class);
        updateTimeout(adminConfiguration);

        configurationProvider.registerEventListener(this, false, false, GeneralAdminConfiguration.class);
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    private static class DispatchedRequest
    {
        RecipeRequest recipeRequest;
        Agent agent;

        public DispatchedRequest(RecipeRequest recipeRequest, Agent agent)
        {
            this.recipeRequest = recipeRequest;
            this.agent = agent;
        }
    }

    private class Dispatcher implements Runnable
    {
        public void run()
        {
            while(!stopRequested)
            {
                DispatchedRequest dispatchedRequest;
                try
                {
                    dispatchedRequest = dispatchedQueue.take();
                }
                catch (InterruptedException e)
                {
                    continue;
                }

                try
                {
                    dispatchedRequest.agent.getService().build(dispatchedRequest.recipeRequest);
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to dispatch recipe: " + e.getMessage(), e);
                    eventManager.publish(new RecipeErrorEvent(this, dispatchedRequest.recipeRequest.getId(), "Unable to dispatch recipe: " + e.getMessage()));
                }
            }
        }
    }
}
