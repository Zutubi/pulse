package com.zutubi.pulse;

import static com.zutubi.pulse.MasterBuildProperties.PROPERTY_CLEAN_BUILD;
import static com.zutubi.pulse.MasterBuildProperties.addRevisionProperties;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.events.RecipeStatusEvent;
import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.*;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.build.BuildStatusEvent;
import com.zutubi.pulse.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.events.system.ConfigurationEventSystemStartedEvent;
import com.zutubi.pulse.events.system.SystemStartedEvent;
import com.zutubi.pulse.scm.ScmChangeEvent;
import com.zutubi.pulse.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.tove.config.project.types.TypeConfiguration;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.config.events.PostSaveEvent;
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
     * The internal queue of dispatch requests.
     */
    private final RequestQueue requestQueue = new RequestQueue();
    
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

    private RecipeDispatchService recipeDispatchService;
    private AgentManager agentManager;
    private EventManager eventManager;
    private GlobalConfiguration globalConfiguration;
    private ScmClientFactory scmClientFactory;
    private ScmContextFactory scmContextFactory;
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
            determineRevision(assignmentRequest);

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

    private void determineRevision(RecipeAssignmentRequest assignmentRequest) throws BuildException, ScmException
    {
        BuildRevision buildRevision = assignmentRequest.getRevision();
        if (!buildRevision.isInitialised())
        {
            // Let's initialise it
            eventManager.publish(new BuildStatusEvent(this, assignmentRequest.getBuild(), "Initialising build revision..."));
            ProjectConfiguration projectConfig = assignmentRequest.getProject().getConfig();
            ScmConfiguration scm = projectConfig.getScm();

            ScmClient client = null;
            try
            {
                ScmContext context = scmContextFactory.createContext(projectConfig.getProjectId(), scm);
                client = scmClientFactory.createClient(scm);
                boolean supportsRevisions = client.getCapabilities().contains(ScmCapability.REVISIONS);
                Revision revision = supportsRevisions ? client.getLatestRevision(context) : new Revision(System.currentTimeMillis());

                // May throw a BuildException
                updateRevision(assignmentRequest, revision);
                eventManager.publish(new BuildStatusEvent(this, assignmentRequest.getBuild(), "Revision initialised to '" + revision.getRevisionString() + "'"));
            }
            finally
            {
                ScmClientUtils.close(client);
            }
        }
    }

    private void updateRevision(RecipeAssignmentRequest assignmentRequest, Revision revision) throws BuildException
    {
        ProjectConfiguration projectConfig = assignmentRequest.getProject().getConfig();
        TypeConfiguration type = projectConfig.getType();
        String pulseFile;
        try
        {
            pulseFile = type.getPulseFile(assignmentRequest.getRequest().getId(), projectConfig, revision, null);
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to retrieve pulse file: " + e.getMessage(), e);
        }
        assignmentRequest.getRevision().update(revision, pulseFile);
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

    void offline(Agent agent)
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
                        for (Agent agent : agentManager.getAvailableAgents())
                        {
                            AgentService service = agent.getService();

                            // can the request be sent to this service?
                            if (request.getHostRequirements().fulfilledBy(request, service))
                            {
                                if (dispatchRequest(request, agent, doneRequests))
                                {
                                    break;
                                }
                            }
                        }
                    }
                }

                requestQueue.removeAll(doneRequests);

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

    private boolean dispatchRequest(RecipeAssignmentRequest request, Agent agent, List<RecipeAssignmentRequest> dispatchedRequests)
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
        eventManager.publish(new RecipeAssignedEvent(this, recipeRequest, agent));
        dispatchedRequests.add(request);

        ExecutionContext context = recipeRequest.getContext();
        addRevisionProperties(context, buildRevision);

        context.addString(NAMESPACE_INTERNAL, PROPERTY_CLEAN_BUILD, Boolean.toString(request.getProject().isForceCleanForAgent(agent.getId())));

        recipeDispatchService.dispatch(new RecipeAssignedEvent(this, recipeRequest, agent));

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
        else if (evt instanceof ScmChangeEvent)
        {
            handleScmChange((ScmChangeEvent) evt);
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
            offline(event.getAgent());
        }
    }

    private void handleScmChange(ScmChangeEvent event)
    {
        List<RecipeAssignmentRequest> rejects = null;
        lock.lock();
        try
        {
            List<RecipeAssignmentRequest> unfulfillable = checkQueueForChanges(event.getSource(), event, requestQueue);
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

    private List<RecipeAssignmentRequest> checkQueueForChanges(ProjectConfiguration changedProject, ScmChangeEvent event, RequestQueue requests)
    {
        List<RecipeAssignmentRequest> unfulfillable = new LinkedList<RecipeAssignmentRequest>();

        for (RecipeAssignmentRequest request : requests)
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
        return new Class[]{
                AgentAvailableEvent.class,
                AgentConnectivityEvent.class,
                AgentResourcesDiscoveredEvent.class,
                ConfigurationEventSystemStartedEvent.class,
                ScmChangeEvent.class,
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

    public void setUnsatisfiableTimeout(int timeout)
    {
        this.unsatisfiableTimeout = timeout;
    }

    public void setSleepInterval(int sleepInterval)
    {
        this.sleepInterval = sleepInterval;
    }

    public void setRecipeDispatchService(RecipeDispatchService recipeDispatchService)
    {
        this.recipeDispatchService = recipeDispatchService;
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

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
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
