package com.zutubi.pulse.master;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.agent.*;
import com.zutubi.pulse.master.events.AgentAvailableEvent;
import com.zutubi.pulse.master.events.AgentOfflineEvent;
import com.zutubi.pulse.master.events.AgentOnlineEvent;
import com.zutubi.pulse.master.events.build.BuildRevisionUpdatedEvent;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.AgentRequirements;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.ChainBootstrapper;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.tove.config.MockConfigurationProvider;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ThreadedRecipeQueueTest extends ZutubiTestCase implements com.zutubi.events.EventListener
{
    private ThreadedRecipeQueue queue;
    private Semaphore buildSemaphore;
    private Semaphore errorSemaphore;
    private Semaphore dispatchedSemaphore;
    private MockAgentManager agentManager;
    private Agent slave1000;
    private List<RecipeErrorEvent> recipeErrors;
    private DefaultEventManager eventManager;
    private RecipeAssignedEvent assignedEvent;
    private RecipeDispatchService recipeDispatchService;

    public ThreadedRecipeQueueTest(String testName)
    {
        super(testName);
        buildSemaphore = new Semaphore(0);
        errorSemaphore = new Semaphore(0);
        dispatchedSemaphore = new Semaphore(0);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager();
        eventManager.register(this);

        recipeDispatchService = new RecipeDispatchService();
        recipeDispatchService.setEventManager(eventManager);
        recipeDispatchService.setThreadFactory(new PulseThreadFactory());
        recipeDispatchService.init();

        agentManager = new MockAgentManager();

        MockConfigurationProvider configurationProvider = new MockConfigurationProvider();
        configurationProvider.insert("test", new GlobalConfiguration());

        queue = new ThreadedRecipeQueue();
        queue.setEventManager(eventManager);
        queue.setAgentManager(agentManager);
        queue.setUnsatisfiableTimeout(-1);
        queue.handleEvent(new ConfigurationEventSystemStartedEvent(configurationProvider));
        queue.handleEvent(new ConfigurationSystemStartedEvent(configurationProvider));
        queue.setThreadFactory(Executors.defaultThreadFactory());
        queue.init();

        slave1000 = createAvailableAgent(1000);
        createAvailableAgent(2000);

        recipeErrors = new LinkedList<RecipeErrorEvent>();
    }

    public void tearDown() throws Exception
    {
        if (!queue.isStopped())
        {
            queue.stop(true);
            while (!queue.isStopped())
            {
                Thread.yield();
            }
        }
        
        eventManager.unregister(this);
        recipeDispatchService.stop(true);
        super.tearDown();
    }

    public void testEnqueue() throws Exception
    {
        // create a 'test' request.
        queue.enqueue(createAssignmentRequest(0));
        assertEquals(1, queue.length());

        Thread.sleep(100);

        assertEquals(1, queue.length());
        assertTrue(queue.isRunning());
        assertFalse(queue.isStopped());

        queue.enqueue(createAssignmentRequest(1));

        Thread.sleep(100);

        assertEquals(2, queue.length());
        assertTrue(queue.isRunning());
        assertFalse(queue.isStopped());

        createAvailableAgent(0);
        createAvailableAgent(1);

        // If it takes longer than 30 seconds, something is wrong
        // Usually this will be pretty immediate.

        awaitBuild();
        awaitBuild();

        Thread.sleep(100);
        assertEquals(0, queue.length());

    }

    public void testStartAgain() throws InterruptedException
    {
        // Enqueue something to make sure the queue is running
        queue.enqueue(createAssignmentRequest(0));
        createAvailableAgent(0);
        awaitBuild();

        try
        {
            queue.start();
            fail();
        }
        catch(IllegalStateException e)
        {

        }
    }

    public void testStopStart() throws Exception
    {
        Thread.sleep(100);
        queue.stop();
        while(queue.isRunning())
        {
            Thread.yield();
        }

        queue.enqueue(createAssignmentRequest(0));
        createAvailableAgent(0);

        // Shouldn't dispatch while stopped
        assertFalse(buildSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS));

        assertEquals(1, queue.length());

        queue.start();
        awaitBuild();
    }

    public void testIncompatibleService() throws Exception
    {
        queue.enqueue(createAssignmentRequest(0));
        createAvailableAgent(1);
        assertFalse(buildSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
    }

    public void testCompatibleAndIncompatibleService() throws Exception
    {
        queue.enqueue(createAssignmentRequest(0));
        createAvailableAgent(1);
        assertFalse(buildSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
        createAvailableAgent(0);
        awaitBuild();
    }

    public void testTwoBuildsSameService() throws Exception
    {
        Agent agent = createAvailableAgent(0);

        queue.enqueue(createAssignmentRequest(0, 1000));
        queue.enqueue(createAssignmentRequest(0, 1001));

        awaitBuild();
        assertFalse(buildSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());

        sendAvailable(agent);
        awaitBuild();
    }

    public void testTwoBuildsSameServiceOnlineAfter() throws Exception
    {
        Agent agent = createAvailableAgent(0);
        queue.enqueue(createAssignmentRequest(0, 1000));
        queue.enqueue(createAssignmentRequest(0, 1001));
        queue.handleEvent(new AgentOnlineEvent(this, agent));

        awaitBuild();
        assertFalse(buildSemaphore.tryAcquire(1, TimeUnit.SECONDS));
        assertEquals(1, queue.length());

        sendAvailable(agent);
        awaitBuild();
    }

    public void testAgentsAreSorted() throws InterruptedException
    {
        final int AGENT_ID_1 = 1;
        final int AGENT_ID_2 = 2;
        final int ACCEPTED_REQUEST_TYPE = 12;
        final int RECIPE_ID_1 = 1000;
        final int RECIPE_ID_2 = 1001;

        queue.setAgentSorter(new AgentSorter()
        {
            public Iterable<Agent> sort(Collection<Agent> agents, RecipeAssignmentRequest request)
            {
                List<Agent> result = new LinkedList<Agent>(agents);
                if (request.getRequest().getId() == RECIPE_ID_2)
                {
                    Collections.reverse(result);
                }

                return result;
            }
        });

        Agent agent1 = createAvailableAgent(ACCEPTED_REQUEST_TYPE, AGENT_ID_1);
        createAvailableAgent(ACCEPTED_REQUEST_TYPE, AGENT_ID_2);

        queue.enqueue(createAssignmentRequest(ACCEPTED_REQUEST_TYPE, RECIPE_ID_1));
        awaitDispatched();
        assertEquals(AGENT_ID_1, assignedEvent.getAgent().getId());
        awaitBuild();
        sendAvailable(agent1);

        queue.enqueue(createAssignmentRequest(ACCEPTED_REQUEST_TYPE, RECIPE_ID_2));
        awaitDispatched();
        assertEquals(AGENT_ID_2, assignedEvent.getAgent().getId());
    }

    public void testThreeServices() throws Exception
    {
        createAvailableAgent(0);
        Agent agent1 = createAvailableAgent(1);
        createAvailableAgent(2);

        queue.enqueue(createAssignmentRequest(0, 1000));
        awaitBuild();

        queue.enqueue(createAssignmentRequest(1, 1001));
        awaitBuild();

        queue.enqueue(createAssignmentRequest(1, 1002));
        assertFalse(buildSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS));

        queue.enqueue(createAssignmentRequest(2, 1003));
        awaitBuild();

        sendAvailable(agent1);
        awaitBuild();
    }

    public void testSnapshot()
    {
        RecipeAssignmentRequest request1 = createAssignmentRequest(0);
        RecipeAssignmentRequest request2 = createAssignmentRequest(0);
        queue.enqueue(request1);
        queue.enqueue(request2);

        List<RecipeAssignmentRequest> snapshot = queue.takeSnapshot();
        assertEquals(2, snapshot.size());
        assertEquals(request1, snapshot.get(0));
        assertEquals(request2, snapshot.get(1));
    }

    public void testSnapshotAfterDispatch() throws Exception
    {
        RecipeAssignmentRequest request1 = createAssignmentRequest(0);
        RecipeAssignmentRequest request2 = createAssignmentRequest(1);
        queue.enqueue(request1);
        queue.enqueue(request2);

        createAvailableAgent(0);
        awaitBuild();

        Thread.sleep(500);

        List<RecipeAssignmentRequest> snapshot = queue.takeSnapshot();
        assertEquals(1, snapshot.size());
        assertEquals(request2, snapshot.get(0));
    }

    public void testCancel() throws Exception
    {
        RecipeAssignmentRequest request = createAssignmentRequest(0, 1);
        queue.enqueue(request);
        assertTrue(queue.cancelRequest(1));
    }

    public void testCancelAfterDelay() throws Exception
    {
        RecipeAssignmentRequest request = createAssignmentRequest(0, 1);
        queue.enqueue(request);
        Thread.sleep(1000);
        assertTrue(queue.cancelRequest(1));
    }

    public void testCancelNoSuchId() throws Exception
    {
        RecipeAssignmentRequest request = createAssignmentRequest(0, 1);
        queue.enqueue(request);
        assertFalse(queue.cancelRequest(2));
    }

    public void testCancelAfterDispatch() throws Exception
    {
        createAvailableAgent(0);

        queue.enqueue(createAssignmentRequest(0, 1000));
        awaitBuild();
        assertFalse(queue.cancelRequest(1000));
    }

    public void testOfflineWhileQueued() throws Exception
    {
        queue.setUnsatisfiableTimeout(0);
        queue.enqueue(createAssignmentRequest(1000, 1000));
        queue.enqueue(createAssignmentRequest(1000, 1001));
        awaitBuild();

        takeOffline(slave1000);
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "No online agent is capable of executing the build stage");
    }

    public void testNoOnlineAgent() throws Exception
    {
        queue.setUnsatisfiableTimeout(0);
        queue.enqueue(createAssignmentRequest(0, 1000));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "No online agent is capable of executing the build stage");
    }

    public void testAgentErrorOnBuild() throws Exception
    {
        Agent agent = createAvailableAgent(0);
        MockAgentService service = (MockAgentService) agent.getService();

        service.setThrowError(true);
        queue.enqueue(createAssignmentRequest(0));
        awaitDispatched();
        awaitBuild();
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals("Unable to dispatch recipe: Error during dispatch", recipeErrors.get(0).getErrorMessage());
        assertEquals(0, queue.length());

        // Make sure we can dispatch another afterwards
        service.setThrowError(false);
        sendAvailable(agent);
        queue.enqueue(createAssignmentRequest(0));
        awaitBuild();
        assertEquals(0, queue.length());
    }

    public void testRevisionUpdateWhileQueued() throws Exception
    {
        ProjectConfiguration projectConfig = createProjectConfig();
        queue.enqueue(createAssignmentRequest(0, 1000, projectConfig));
        RecipeAssignmentRequest queuedRequest = createAssignmentRequest(0, 1001, projectConfig);
        queue.enqueue(queuedRequest);

        Agent agent = createAvailableAgent(0);
        awaitBuild();
        awaitDispatched();

        BuildRevision queuedRevision = queuedRequest.getRevision();
        updateRevision(queuedRevision, 98);

        queue.handleEvent(new BuildRevisionUpdatedEvent(this, queuedRequest.getBuild(), queuedRevision));
        sendAvailable(agent);
        awaitBuild();
        awaitDispatched();
        assertEquals(1001, assignedEvent.getRequest().getId());
    }

    public void testRevisionUpdateMakesUnfulfillable() throws Exception
    {
        ProjectConfiguration projectConfig = createProjectConfig(12);
        queue.enqueue(createAssignmentRequest(0, 1000, projectConfig));
        RecipeAssignmentRequest queuedRequest = createAssignmentRequest(0, 1001, projectConfig);
        queue.enqueue(queuedRequest);

        queue.setUnsatisfiableTimeout(0);
        createAvailableAgent(0);
        awaitBuild();

        // Negative revision will be rejected by mock
        BuildRevision queuedRevision = queuedRequest.getRevision();
        updateRevision(queuedRevision, -1);

        queue.handleEvent(new BuildRevisionUpdatedEvent(this, queuedRequest.getBuild(), queuedRevision));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "No online agent is capable of executing the build stage");
    }

    public void testRevisionUpdateOtherBuild() throws Exception
    {
        ProjectConfiguration projectConfig = createProjectConfig(12);
        queue.enqueue(createAssignmentRequest(0, 1000, projectConfig));
        RecipeAssignmentRequest queuedRequest = createAssignmentRequest(0, 1001, projectConfig);
        queue.enqueue(queuedRequest);

        queue.setUnsatisfiableTimeout(0);
        Agent agent = createAvailableAgent(0);
        awaitBuild();
        awaitDispatched();

        // Negative revision will be rejected by mock
        BuildResult anotherBuild = new BuildResult();
        anotherBuild.setId(queuedRequest.getBuild().getId() + 1);
        BuildRevision anotherRevision = new BuildRevision();
        updateRevision(anotherRevision, -1);
        queue.handleEvent(new BuildRevisionUpdatedEvent(this, anotherBuild, anotherRevision));

        // Despite updating another build to a rejected revision, this build
        // still goes through.
        sendAvailable(agent);
        awaitBuild();
        awaitDispatched();
        assertEquals(1001, assignedEvent.getRequest().getId());
    }

    private void updateRevision(BuildRevision queuedRevision, int revision)
    {
        queuedRevision.lock();
        try
        {
            queuedRevision.update(createRevision(revision));
        }
        finally
        {
            queuedRevision.unlock();
        }
    }

    private ProjectConfiguration createProjectConfig()
    {
        return createProjectConfig(0);
    }

    private ProjectConfiguration createProjectConfig(final int projectId)
    {
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setProjectId(projectId);
        projectConfig.setType(new CustomTypeConfiguration());
        return projectConfig;
    }

    public void testOfflineRemovedFromAvailable() throws InterruptedException
    {
        Agent offAgent = createAvailableAgent(0);
        agentManager.unavailable(offAgent);
        queue.offline(offAgent);
        queue.enqueue(createAssignmentRequest(0, 1));
        assertFalse(buildSemaphore.tryAcquire(3, TimeUnit.SECONDS));
    }

    public void testTimedOutRequest() throws Exception
    {
        queue.setSleepInterval(1);
        queue.setUnsatisfiableTimeout(1);
        queue.enqueue(createAssignmentRequest(0, 111));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(111, "Recipe request timed out waiting for a capable agent to become available");
    }

    public void testNoTimeout() throws Exception
    {
        queue.setSleepInterval(1);
        queue.setUnsatisfiableTimeout(-1);
        queue.enqueue(createAssignmentRequest(0, 111));
        assertFalse(errorSemaphore.tryAcquire(3, TimeUnit.SECONDS));
    }

    public void testTimedOutAfterAgentOffline() throws Exception
    {
        queue.setUnsatisfiableTimeout(1);
        queue.setSleepInterval(1);
        queue.enqueue(createAssignmentRequest(1000, 1000));
        queue.enqueue(createAssignmentRequest(1000, 1001));
        awaitBuild();

        takeOffline(slave1000);
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "Recipe request timed out waiting for a capable agent to become available");
    }

    public void testNoTimeoutAgentOffline() throws Exception
    {
        queue.setUnsatisfiableTimeout(-1);
        queue.setSleepInterval(1);
        queue.enqueue(createAssignmentRequest(1000, 1000));
        queue.enqueue(createAssignmentRequest(1000, 1001));
        awaitBuild();

        takeOffline(slave1000);
        assertFalse(errorSemaphore.tryAcquire(3, TimeUnit.SECONDS));
        assertNoError(1001);
    }

    public void testRevisionUpdateMakesTimeout() throws Exception
    {
        ProjectConfiguration projectConfig = createProjectConfig(12);
        queue.enqueue(createAssignmentRequest(0, 1000, projectConfig));
        RecipeAssignmentRequest queuedRequest = createAssignmentRequest(0, 1001, projectConfig);
        queue.enqueue(queuedRequest);

        queue.setSleepInterval(1);
        createAvailableAgent(0);
        awaitBuild();

        queue.setUnsatisfiableTimeout(1);

        // Negative revision will be rejected by mock
        BuildRevision queuedRevision = queuedRequest.getRevision();
        updateRevision(queuedRevision, -1);

        queue.handleEvent(new BuildRevisionUpdatedEvent(this, queuedRequest.getBuild(), queuedRevision));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "Recipe request timed out waiting for a capable agent to become available");
    }

    //-----------------------------------------------------------------------
    // Helpers and mocks
    //-----------------------------------------------------------------------

    private void awaitBuild() throws InterruptedException
    {
        assertTrue(buildSemaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    private void awaitDispatched() throws InterruptedException
    {
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    private void assertRecipeError(long id, String message)
    {
        for(RecipeErrorEvent error: recipeErrors)
        {
            if(error.getRecipeId() == id)
            {
                assertEquals(message, error.getErrorMessage());
                return;
            }
        }

        fail("Recipe error with given id not found");
    }

    private void assertNoError(long id)
    {
        for(RecipeErrorEvent error: recipeErrors)
        {
            if(error.getRecipeId() == id)
            {
                fail("Found error for id that should have no errors");
            }
        }
    }

    private void takeOffline(Agent agent)
    {
        agent.updateStatus(new SlaveStatus(PingStatus.OFFLINE, "oops"));
        agentManager.offline(agent);
        queue.handleEvent(new AgentOfflineEvent(this, agent));
    }

    private void sendAvailable(Agent agent)
    {
        agentManager.available(agent);
        queue.handleEvent(new AgentAvailableEvent(this, agent));
    }

    public void sendRecipeError(long id)
    {
        queue.handleEvent(new RecipeErrorEvent(this, id, "test"));
    }

    private AgentConfiguration createAgentConfig(long handle)
    {
        AgentConfiguration result = new AgentConfiguration();
        result.setHandle(handle);
        result.setName("name" + handle);
        result.setHost("host" + handle);
        return result;
    }

    private Agent createAvailableAgent(long typeAndId)
    {
        return createAvailableAgent(typeAndId, typeAndId);
    }

    private Agent createAvailableAgent(long type, long id)
    {
        AgentConfiguration agentConfig = createAgentConfig(id);
        AgentState agentState = new AgentState();
        agentState.setId(id);
        DefaultAgent agent = new DefaultAgent(agentConfig, agentState, new MockAgentService(type));
        agent.updateStatus(new SlaveStatus(PingStatus.IDLE, 0, false));
        agentManager.addAgent(agent);
        agentManager.online(agent);
        queue.handleEvent(new AgentOnlineEvent(this, agent));
        agentManager.available(agent);
        queue.handleEvent(new AgentAvailableEvent(this, agent));
        return agent;
    }

    private RecipeAssignmentRequest createAssignmentRequest(int type, long id)
    {
        return createAssignmentRequest(type, id, createProjectConfig());
    }

    private RecipeAssignmentRequest createAssignmentRequest(int type, long id, ProjectConfiguration projectConfig)
    {
        Project project = new Project();
        project.setConfig(projectConfig);
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, 100, false);
        AgentRequirements requirements = new MockAgentRequirements(type);
        PulseExecutionContext context = new PulseExecutionContext();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_PROJECT, "project");
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, Long.toString(id));
        RecipeRequest request = new RecipeRequest(context);
        request.setBootstrapper(new ChainBootstrapper());
        BuildRevision revision = new BuildRevision();
        revision.lock();
        try
        {
            revision.update(new Revision("1"));
        }
        finally
        {
            revision.unlock();
        }
        return new RecipeAssignmentRequest(project, requirements, null, revision, request, result);
    }

    private RecipeAssignmentRequest createAssignmentRequest(int type)
    {
        return createAssignmentRequest(type, -1);
    }

    public void handleEvent(Event evt)
    {
        if(evt instanceof RecipeErrorEvent)
        {
            recipeErrors.add((RecipeErrorEvent) evt);
            errorSemaphore.release();
        }
        else
        {
            assignedEvent = (RecipeAssignedEvent)evt;
            agentManager.unavailable(assignedEvent.getAgent());
            dispatchedSemaphore.release();
            recipeDispatchService.dispatch(assignedEvent);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeErrorEvent.class, RecipeAssignedEvent.class};
    }

    class MockAgentManager implements AgentManager
    {
        private Map<Long, Agent> agents = new TreeMap<Long, Agent>();
        private List<Agent> onlineAgents = new LinkedList<Agent>();
        private List<Agent> availableAgents = new LinkedList<Agent>();

        public synchronized List<Agent> getAllAgents()
        {
            return new LinkedList<Agent>(agents.values());
        }

        public synchronized List<Agent> getOnlineAgents()
        {
            return new LinkedList<Agent>(onlineAgents);
        }

        public synchronized List<Agent> getAvailableAgents()
        {
            return new LinkedList<Agent>(availableAgents);
        }

        public synchronized Agent getAgent(long handle)
        {
            return agents.get(handle);
        }

        public synchronized Agent getAgent(AgentConfiguration agent)
        {
            return agents.get(agent.getHandle());
        }

        public synchronized void addAgent(Agent agent)
        {
            agents.put(agent.getConfig().getHandle(), agent);
        }

        public synchronized void online(Agent agent)
        {
            if (!onlineAgents.contains(agent))
            {
                onlineAgents.add(agent);
            }
        }

        public synchronized void offline(Agent agent)
        {
            onlineAgents.remove(agent);
        }

        public synchronized void available(Agent agent)
        {
            if (!availableAgents.contains(agent))
            {
                availableAgents.add(agent);
                // Ensure a consistent sorting order, so we can test the
 	            // agent sorter is actually doing something.
                Collections.sort(availableAgents, new Comparator<Agent>()
                {
                    public int compare(Agent o1, Agent o2)
                    {
                        return (int) (o1.getId() - o2.getId());
                    }
                });
            }
        }

        public synchronized void unavailable(Agent agent)
        {
            availableAgents.remove(agent);
        }

        public void pingAgent(AgentConfiguration agentConfig)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void pingAgents()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public int getAgentCount()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void upgradeStatus(UpgradeStatus upgradeStatus)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public Agent getAgent(String name)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void setEnableState(Agent agent, AgentState.EnableState state)
        {
            throw new RuntimeException("Method not yet implemented.");
        }
    }

    class MockAgentService implements AgentService
    {
        private long type;
        private boolean acceptBuild = true;
        private boolean throwError = false;

        public MockAgentService(long type)
        {
            this.type = type;
        }

        public int ping()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public SlaveStatus getStatus(String masterLocation)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public boolean updateVersion(String masterBuild, String masterUrl, long handle, String packageUrl, long packageSize)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public List<ResourceConfiguration> discoverResources()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public SystemInfo getSystemInfo()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public List<CustomLogRecord> getRecentMessages()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public AgentConfiguration getAgentConfig()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public boolean hasResource(ResourceRequirement requirement)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean build(RecipeRequest request)
        {
            buildSemaphore.release();
            if(throwError)
            {
                throw new RuntimeException("Error during dispatch");
            }
            return acceptBuild;
        }

        public void collectResults(AgentRecipeDetails recipeDetails, File outputDest, File workDest)
        {
            throw new RuntimeException("Not implemented");
        }

        public void cleanup(AgentRecipeDetails recipeDetails)
        {
            throw new RuntimeException("Not implemented");
        }

        public void terminateRecipe(long recipeId)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String getHostName()
        {
            return "[mock]";
        }

        public void garbageCollect()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public String getUrl()
        {
            return null;
        }

        public long getType()
        {
            return type;
        }

        public void setAcceptBuild(boolean acceptBuild)
        {
            this.acceptBuild = acceptBuild;
        }

        public void setThrowError(boolean throwError)
        {
            this.throwError = throwError;
        }
    }

    class MockAgentRequirements implements AgentRequirements
    {
        private long type;

        public MockAgentRequirements(long type)
        {
            this.type = type;
        }

        public AgentRequirements copy()
        {
            return new MockAgentRequirements(type);
        }

        public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service)
        {
            return (((MockAgentService) service).getType() == type) &&
                    Long.valueOf(request.getRevision().getRevision().getRevisionString()) >= 0;
        }

        public String getSummary()
        {
            return "mock";
        }
    }

    private Revision createRevision(long rev)
    {
        return new Revision(Long.toString(rev));
    }
}