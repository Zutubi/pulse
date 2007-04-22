package com.zutubi.pulse;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.SlaveAgent;
import com.zutubi.pulse.agent.Status;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.scm.*;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.pulse.servercore.config.ScmConfiguration;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 */
public class ThreadedRecipeQueueTest extends TestCase implements EventListener
{
    private static final String PULSE_FILE = "<xml version=\"1.0\"><project default-recipe=\"default\"><recipe name=\"default\"/></project>";

    private ThreadedRecipeQueue queue;
    private Semaphore semaphore;
    private Semaphore errorSemaphore;
    private Semaphore dispatchedSemaphore;
    private ThreadedRecipeQueueTest.MockAgentManager agentManager;
    private Slave slave1000;
    private Slave slave2000;
    private Slave slave3000;
    private List<RecipeErrorEvent> recipeErrors;
    private DefaultEventManager eventManager;
    private RecipeDispatchedEvent dispatchedEvent;

    public ThreadedRecipeQueueTest(String testName)
    {
        super(testName);
        semaphore = new Semaphore(0);
        errorSemaphore = new Semaphore(0);
        dispatchedSemaphore = new Semaphore(0);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        eventManager = new DefaultEventManager();
        eventManager.register(this);

        agentManager = new MockAgentManager();
        slave1000 = createSlave(1000);
        agentManager.addSlave(slave1000);
        slave2000 = createSlave(2000);
        agentManager.addSlave(slave2000);
        slave3000 = createSlave(3000);
        agentManager.addSlave(slave3000);

        queue = new ThreadedRecipeQueue();
        queue.setEventManager(eventManager);
        queue.setAgentManager(agentManager);
        queue.setUnsatisfiableTimeout(-1);
        queue.init();

        recipeErrors = new LinkedList<RecipeErrorEvent>();
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        eventManager.unregister(this);
        slave1000 = null;
        slave2000 = null;
        slave3000 = null;
        agentManager = null;
        eventManager = null;
        semaphore = null;
        errorSemaphore = null;
        dispatchedSemaphore = null;
        queue = null;
        recipeErrors = null;
        super.tearDown();
    }

    public void testEnqueue() throws Exception
    {
        // create a 'test' request.
        queue.enqueue(createDispatchRequest(0));
        assertEquals(1, queue.length());

        Thread.sleep(100);

        assertEquals(1, queue.length());
        assertTrue(queue.isRunning());
        assertFalse(queue.isStopped());

        queue.enqueue(createDispatchRequest(1));

        Thread.sleep(100);

        assertEquals(2, queue.length());
        assertTrue(queue.isRunning());
        assertFalse(queue.isStopped());

        queue.online(createAvailableAgent(0));
        queue.online(createAvailableAgent(1));

        // If it takes longer than 30 seconds, something is wrong
        // Usually this will be pretty immediate.
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        Thread.sleep(100);
        assertEquals(0, queue.length());

    }

    public void testStartAgain() throws InterruptedException
    {
        // Enqueue something to make sure the queue is running
        queue.enqueue(createDispatchRequest(0));
        queue.online(createAvailableAgent(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

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

        queue.enqueue(createDispatchRequest(0));
        queue.online(createAvailableAgent(0));

        // Shouldn't dispatch while stopped
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));

        assertEquals(1, queue.length());

        queue.start();
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testIncompatibleService() throws Exception
    {
        queue.enqueue(createDispatchRequest(0));
        queue.online(createAvailableAgent(1));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
    }

    public void testCompatibleAndIncompatibleService() throws Exception
    {
        queue.enqueue(createDispatchRequest(0));
        queue.online(createAvailableAgent(1));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
        queue.online(createAvailableAgent(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testTwoBuildsSameService() throws Exception
    {
        queue.online(createAvailableAgent(0));

        queue.enqueue(createDispatchRequest(0, 1000));
        queue.enqueue(createDispatchRequest(0, 1001));

        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());

        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testTwoBuildsSameServiceOnlineAfter() throws Exception
    {
        Agent agent = createAvailableAgent(0);
        queue.online(agent);
        queue.enqueue(createDispatchRequest(0, 1000));
        queue.enqueue(createDispatchRequest(0, 1001));
        queue.online(agent);

        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(semaphore.tryAcquire(1, TimeUnit.SECONDS));
        assertEquals(1, queue.length());

        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testRecipeError() throws Exception
    {
        queue.online(createAvailableAgent(0));

        queue.enqueue(createDispatchRequest(0, 1000));
        queue.enqueue(createDispatchRequest(0, 1001));

        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());

        sendRecipeError(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testIgnoresUnknownRecipe() throws Exception
    {
        queue.online(createAvailableAgent(0));

        queue.enqueue(createDispatchRequest(0, 1000));
        queue.enqueue(createDispatchRequest(0, 1001));

        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());

        sendRecipeCompleted(22);
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
    }

    public void testThreeServices() throws Exception
    {
        queue.online(createAvailableAgent(0));
        queue.online(createAvailableAgent(1));
        queue.online(createAvailableAgent(2));

        queue.enqueue(createDispatchRequest(0, 1000));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.enqueue(createDispatchRequest(1, 1001));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.enqueue(createDispatchRequest(1, 1002));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));

        queue.enqueue(createDispatchRequest(2, 1003));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        sendRecipeCompleted(1001);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testSnapshot()
    {
        RecipeDispatchRequest request1 = createDispatchRequest(0);
        RecipeDispatchRequest request2 = createDispatchRequest(0);
        queue.enqueue(request1);
        queue.enqueue(request2);

        List<RecipeDispatchRequest> snapshot = queue.takeSnapshot();
        assertEquals(2, snapshot.size());
        assertEquals(request1, snapshot.get(0));
        assertEquals(request2, snapshot.get(1));
    }

    public void testSnapshotAfterDispatch() throws Exception
    {
        RecipeDispatchRequest request1 = createDispatchRequest(0);
        RecipeDispatchRequest request2 = createDispatchRequest(1);
        queue.enqueue(request1);
        queue.enqueue(request2);

        queue.online(createAvailableAgent(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        List<RecipeDispatchRequest> snapshot = queue.takeSnapshot();
        assertEquals(1, snapshot.size());
        assertEquals(request2, snapshot.get(0));
    }

    public void testCancel() throws Exception
    {
        RecipeDispatchRequest request = createDispatchRequest(0, 1);
        queue.enqueue(request);
        assertTrue(queue.cancelRequest(1));
    }

    public void testCancelAfterDelay() throws Exception
    {
        RecipeDispatchRequest request = createDispatchRequest(0, 1);
        queue.enqueue(request);
        Thread.sleep(1000);
        assertTrue(queue.cancelRequest(1));
    }

    public void testCancelNoSuchId() throws Exception
    {
        RecipeDispatchRequest request = createDispatchRequest(0, 1);
        queue.enqueue(request);
        assertFalse(queue.cancelRequest(2));
    }

    public void testCancelAfterDispatch() throws Exception
    {
        queue.online(createAvailableAgent(0));

        queue.enqueue(createDispatchRequest(0, 1000));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(queue.cancelRequest(1000));
    }

    public void testOfflineDuringExecution() throws Exception
    {
        queue.enqueue(createDispatchRequest(1000, 1000));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        sendOfflineEvent(slave1000);
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "Connection to agent lost during recipe execution");
        assertEquals(0, queue.executingCount());
    }

    public void testOfflineNotExecuting() throws Exception
    {
        queue.enqueue(createDispatchRequest(1000, 1000));
        sendOfflineEvent(slave2000);
        Thread.sleep(500);
        assertEquals(1, queue.executingCount());
    }

    public void testOfflineWhileQueued() throws Exception
    {
        queue.setUnsatisfiableTimeout(0);
        queue.enqueue(createDispatchRequest(1000, 1000));
        queue.enqueue(createDispatchRequest(1000, 1001));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        sendOfflineEvent(slave1000);
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "No online agent is capable of executing the build stage");
    }

    public void testNoOnlineAgent() throws Exception
    {
        queue.setUnsatisfiableTimeout(0);
        queue.enqueue(createDispatchRequest(0, 1000));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "No online agent is capable of executing the build stage");
    }

//    public void testAgentRejectsBuild() throws Exception
//    {
//        Agent agent = createAvailableAgent(0);
//        queue.online(agent);
//        MockBuildService service = (MockBuildService) agent.getBuildService();
//
//        service.setAcceptBuild(false);
//        queue.enqueue(createDispatchRequest(0));
//        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
//
//        // Still not dispatched
//        assertEquals(1, queue.length());
//
//        service.setAcceptBuild(true);
//        sendOnlineEvent(slave1000);
//        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
//
//        // Now dispatched
//        assertEquals(0, queue.length());
//    }

    public void testAgentErrorOnBuild() throws Exception
    {
        Agent agent = createAvailableAgent(0);
        queue.online(agent);
        MockBuildService service = (MockBuildService) agent.getBuildService();

        service.setThrowError(true);
        queue.enqueue(createDispatchRequest(0));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals("Unable to dispatch recipe: Error during dispatch", recipeErrors.get(0).getErrorMessage());
        assertEquals(0, queue.length());

        // Make sure we can dispatch another afterwards
        service.setThrowError(false);
        queue.enqueue(createDispatchRequest(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(0, queue.length());
    }

    public void testErrorDeterminingRevision() throws Exception
    {
        MockScm scm = new MockScm(true);
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setScm(scm);
        queue.online(createAvailableAgent(0));
        queue.enqueue(createDispatchRequest(0, 1000, projectConfig));

        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "Unable to determine revision to build: test");
    }

    public void testFixedRevision() throws Exception
    {
        RecipeDispatchRequest request = createDispatchRequest(0);
        request.getRevision().update(new NumericalRevision(88), getPulseFileForRevision(88));
        request.getRevision().apply(request.getRequest());

        queue.online(createAvailableAgent(0));
        queue.enqueue(request);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(dispatchedEvent.getRequest().getPulseFileSource(), getPulseFileForRevision(88));
    }

    public void testChangeWhileQueued() throws Exception
    {
        ProjectConfiguration projectConfig = createProjectConfig();
        queue.enqueue(createDispatchRequest(0, 1000, projectConfig));
        queue.enqueue(createDispatchRequest(0, 1001, projectConfig));

        queue.online(createAvailableAgent(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.handleEvent(new SCMChangeEvent(projectConfig, new NumericalRevision(98), new NumericalRevision(1)));
        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRequest().getId());
        assertEquals(getPulseFileForRevision(98), dispatchedEvent.getRequest().getPulseFileSource());
    }

    public void testChangeOtherScmWhileQueued() throws Exception
    {
        ProjectConfiguration projectConfig = createProjectConfig(12);
        queue.enqueue(createDispatchRequest(0, 1000, projectConfig));
        queue.enqueue(createDispatchRequest(0, 1001, projectConfig));

        queue.online(createAvailableAgent(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.handleEvent(new SCMChangeEvent(createProjectConfig(), new NumericalRevision(98), new NumericalRevision(1)));
        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRecipeId());
        assertEquals(getPulseFileForRevision(1), dispatchedEvent.getRequest().getPulseFileSource());
    }

    private ProjectConfiguration createProjectConfig()
    {
        return createProjectConfig(0);
    }

    private ProjectConfiguration createProjectConfig(int projectId)
    {
        MockScm scm = new MockScm();
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setScm(scm);
        projectConfig.setProjectId(projectId);
        return projectConfig;
    }

    public void testChangeButFixed() throws Exception
    {
        queue.online(createAvailableAgent(0));

        ProjectConfiguration projectConfig = createProjectConfig();
        queue.enqueue(createDispatchRequest(0, 1000, projectConfig));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        RecipeDispatchRequest request = createDispatchRequest(0, 1001);
        request.getRevision().update(new NumericalRevision(5), getPulseFileForRevision(5));
        request.getRevision().apply(request.getRequest());
        queue.enqueue(request);

        queue.handleEvent(new SCMChangeEvent(projectConfig, new NumericalRevision(98), new NumericalRevision(1)));
        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRecipeId());
        assertEquals(getPulseFileForRevision(5), dispatchedEvent.getRequest().getPulseFileSource());
    }

    public void testChangeMakesUnfulfillable() throws Exception
    {
        ProjectConfiguration projectConfig = createProjectConfig(12);
        queue.enqueue(createDispatchRequest(0, 1000, projectConfig));
        queue.enqueue(createDispatchRequest(0, 1001, projectConfig));

        queue.setUnsatisfiableTimeout(0);
        Agent agent = createAvailableAgent(0);
        queue.online(agent);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));


        // Negative revision will be rejected by mock
        queue.handleEvent(new SCMChangeEvent(projectConfig, new NumericalRevision(-1), new NumericalRevision(1)));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "No online agent is capable of executing the build stage");
    }

    public void testChangeCantNewPulseFile() throws Exception
    {
        queue.online(createAvailableAgent(0));

        ProjectConfiguration projectConfig = createProjectConfig();
        queue.enqueue(createDispatchRequest(0, 1000, projectConfig));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.enqueue(createDispatchRequest(0, 1001, projectConfig));

        queue.handleEvent(new SCMChangeEvent(projectConfig, new NumericalRevision(0), new NumericalRevision(1)));
        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRecipeId());
        assertEquals(getPulseFileForRevision(1), dispatchedEvent.getRequest().getPulseFileSource());
    }

    public void testOfflineRemovedFromAvailable() throws InterruptedException
    {
        Agent offAgent = createAvailableAgent(0);
        queue.online(offAgent);
        queue.offline(offAgent);
        queue.enqueue(createDispatchRequest(0, 1));
        assertFalse(semaphore.tryAcquire(3, TimeUnit.SECONDS));
    }

    public void testTimedOutRequest() throws Exception
    {
        queue.setSleepInterval(1);
        queue.setUnsatisfiableTimeout(1);
        queue.enqueue(createDispatchRequest(0, 111));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(111, "Recipe request timed out waiting for a capable agent to become available");
    }

    public void testNoTimeout() throws Exception
    {
        queue.setSleepInterval(1);
        queue.setUnsatisfiableTimeout(-1);
        queue.enqueue(createDispatchRequest(0, 111));
        assertFalse(errorSemaphore.tryAcquire(3, TimeUnit.SECONDS));
    }

    public void testTimedOutAfterAgentOffline() throws Exception
    {
        queue.setUnsatisfiableTimeout(1);
        queue.setSleepInterval(1);
        queue.enqueue(createDispatchRequest(1000, 1000));
        queue.enqueue(createDispatchRequest(1000, 1001));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        sendOfflineEvent(slave1000);
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "Recipe request timed out waiting for a capable agent to become available");
    }

    public void testNoTimeoutAgentOffline() throws Exception
    {
        queue.setUnsatisfiableTimeout(-1);
        queue.setSleepInterval(1);
        queue.enqueue(createDispatchRequest(1000, 1000));
        queue.enqueue(createDispatchRequest(1000, 1001));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        sendOfflineEvent(slave1000);
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(errorSemaphore.tryAcquire(3, TimeUnit.SECONDS));
        assertNoError(1001);
    }

    public void testChangeMakesTimeout() throws Exception
    {
        ProjectConfiguration projectConfig = createProjectConfig(12);
        queue.enqueue(createDispatchRequest(0, 1000, projectConfig));
        queue.enqueue(createDispatchRequest(0, 1001, projectConfig));

        queue.setSleepInterval(1);
        queue.setUnsatisfiableTimeout(1);
        Agent agent = createAvailableAgent(0);
        queue.online(agent);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        // Negative revision will be rejected by mock
        queue.handleEvent(new SCMChangeEvent(projectConfig, new NumericalRevision(-1), new NumericalRevision(1)));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "Recipe request timed out waiting for a capable agent to become available");
    }

    //-----------------------------------------------------------------------
    // Helpers and mocks
    //-----------------------------------------------------------------------

    private String getPulseFileForRevision(long revision)
    {
        return PULSE_FILE + "<!--" + revision + "-->";
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

    private void sendOnlineEvent(Slave slave)
    {
        SlaveAgent a = (SlaveAgent) agentManager.getAgent(slave);
        AgentStatusEvent event = new AgentStatusEvent(this, Status.OFFLINE, a);
        queue.handleEvent(event);
    }

    private void sendOfflineEvent(Slave slave)
    {
        SlaveAgent a = (SlaveAgent) agentManager.getAgent(slave);
        a.updateStatus(new SlaveStatus(Status.OFFLINE, "oops"));
        SlaveAgentRemovedEvent event = new SlaveAgentRemovedEvent(this, a);
        queue.handleEvent(event);
    }

    private void sendRecipeCompleted(long id)
    {
        RecipeResult result = new RecipeResult();
        result.setId(id);
        queue.handleEvent(new RecipeCompletedEvent(this, result));
    }

    public void sendRecipeError(long id)
    {
        queue.handleEvent(new RecipeErrorEvent(this, id, "test"));
    }

    private Slave createSlave(long id)
    {
        Slave result = new Slave("name" + id, "host" + id);
        result.setId(id);
        return result;
    }

    private Agent createAvailableAgent(long type)
    {
        SlaveAgent slaveAgent = new SlaveAgent(createSlave(type), null, null, new MockBuildService(type));
        slaveAgent.updateStatus(new SlaveStatus(Status.IDLE, 0));
        return slaveAgent;
    }

    private RecipeDispatchRequest createDispatchRequest(int type, long id)
    {
        return createDispatchRequest(type, id, createProjectConfig());
    }

    private RecipeDispatchRequest createDispatchRequest(int type, long id, ProjectConfiguration projectConfig)
    {
        Project project = new Project("test", "test description", new MockPulseFileDetails());
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, new BuildSpecification("spec"), 100, false);
        BuildHostRequirements requirements = new MockBuildHostRequirements(type);
        RecipeRequest request = new RecipeRequest("project", "spec", id, null, null, null, false, null, new LinkedList<ResourceProperty>());
        request.setBootstrapper(new ChainBootstrapper());
        return new RecipeDispatchRequest(requirements, new BuildRevision(), request, projectConfig, result);
    }

    private RecipeDispatchRequest createDispatchRequest(int type)
    {
        return createDispatchRequest(type, -1);
    }

    public void handleEvent(Event evt)
    {
        System.out.println("evt = " + evt.toString());
        if(evt instanceof RecipeErrorEvent)
        {
            recipeErrors.add((RecipeErrorEvent) evt);
            errorSemaphore.release();
        }
        else
        {
            dispatchedEvent = (RecipeDispatchedEvent)evt;
            dispatchedSemaphore.release();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeErrorEvent.class, RecipeDispatchedEvent.class};
    }

    class MockScm extends ScmConfiguration
    {
        private boolean throwError = false;

        public MockScm()
        {
        }

        public MockScm(boolean throwError)
        {
            this.throwError = throwError;
        }

        public void setThrowError(boolean throwError)
        {
            this.throwError = throwError;
        }

        public ScmClient createClient() throws SCMException
        {
            return new MockScmClient(throwError);
        }

        public String getType()
        {
            return "mock";
        }
    }

    class MockScmClient implements ScmClient
    {
        private boolean throwError = false;

        public MockScmClient()
        {
        }

        public MockScmClient(boolean throwError)
        {
            this.throwError = throwError;
        }

        public void setThrowError(boolean throwError)
        {
            this.throwError = throwError;
        }

        public Set<SCMCapability> getCapabilities()
        {
            return new HashSet<SCMCapability>(Arrays.asList(SCMCapability.values()));            
        }

        public Map<String, String> getServerInfo() throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String getUid() throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String getLocation()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void testConnection() throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public Revision checkout(String id, File toDirectory, Revision revision, SCMCheckoutEventHandler handler) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public InputStream checkout(Revision revision, String file) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public List<Changelist> getChanges(Revision from, Revision to) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public List<Revision> getRevisionsSince(Revision from) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean hasChangedSince(Revision since) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public Revision getLatestRevision() throws SCMException
        {
            if(throwError)
            {
                throw new SCMException("test");
            }
            else
            {
                return new NumericalRevision(1);
            }
        }

        public SCMFile getFile(String path) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public List<SCMFile> getListing(String path) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void update(String id, File workDir, Revision rev, SCMCheckoutEventHandler handler) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void tag(Revision revision, String name, boolean moveExisting) throws SCMException
        {
            throw new RuntimeException("Method not implemented");
        }

        public Map<String, String> getProperties(String id, File dir) throws SCMException
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void storeConnectionDetails(File outputDir) throws SCMException, IOException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public FileStatus.EOLStyle getEOLPolicy() throws SCMException
        {
            return FileStatus.EOLStyle.BINARY;
        }

        public Revision getRevision(String revision) throws SCMException
        {
            throw new RuntimeException("Method not yet implemented.");
        }
    }


    class MockPulseFileDetails extends PulseFileDetails
    {
        public PulseFileDetails copy()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean isBuiltIn()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String getType()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public Properties getProperties()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String getPulseFile(long id, ProjectConfiguration projectConfig, Project project, Revision revision, PatchArchive patch)
        {
            long number = ((NumericalRevision) revision).getRevisionNumber();
            if(number == 0)
            {
                throw new BuildException("test");
            }

            return getPulseFileForRevision(number);
        }
    }

    class MockAgentManager implements AgentManager
    {
        private Map<Long, Agent> onlineAgents = new TreeMap<Long, Agent>();

        public List<Agent> getAllAgents()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public List<Agent> getOnlineAgents()
        {
            return new LinkedList<Agent>(onlineAgents.values());
        }

        public Agent getAgent(Slave slave)
        {
            if(slave == null)
            {
                return onlineAgents.get(0L);
            }
            else
            {
                return onlineAgents.get(slave.getId());
            }
        }

        public void pingSlave(Slave slave)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void pingSlaves()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public int getAgentCount()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void slaveAdded(long id)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void slaveChanged(long id)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void slaveDeleted(long id)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void upgradeStatus(UpgradeStatus upgradeStatus)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean agentExists(String name)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public Agent getAgent(String name)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void enableMasterAgent()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void disableMasterAgent()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void addSlave(Slave slave)
        {
            SlaveAgent agent = new SlaveAgent(slave, null, null, new MockBuildService(slave.getId()));
            agent.updateStatus(new SlaveStatus(Status.IDLE, 0));
            onlineAgents.put(slave.getId(), agent);
        }

        public void enableSlave(Slave slave)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void disableSlave(Slave slave)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void setSlaveState(Slave slave, Slave.EnableState state)
        {
            throw new RuntimeException("Method not yet implemented.");
        }
    }

    class MockBuildService implements BuildService
    {
        private long type;
        private boolean acceptBuild = true;
        private boolean throwError = false;

        public MockBuildService(long type)
        {
            this.type = type;
        }

        public boolean hasResource(String resource, String version)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean build(RecipeRequest request, BuildContext context)
        {
            semaphore.release();
            if(throwError)
            {
                throw new RuntimeException("Error during dispatch");
            }
            return acceptBuild;
        }

        public void collectResults(String project, String spec, long recipeId, boolean incremental, File outputDest, File workDest)
        {
        }

        public void cleanup(String project, String spec, long recipeId, boolean incremental)
        {
        }

        public void terminateRecipe(long recipeId)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String getHostName()
        {
            return "[mock]";
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

    class MockBuildHostRequirements implements BuildHostRequirements
    {
        private long type;

        public MockBuildHostRequirements(long type)
        {
            this.type = type;
        }

        public BuildHostRequirements copy()
        {
            return new MockBuildHostRequirements(type);
        }

        public boolean fulfilledBy(RecipeDispatchRequest request, BuildService service)
        {
            return (((MockBuildService) service).getType() == type) &&
                    (((NumericalRevision)request.getRevision().getRevision()).getRevisionNumber() >= 0);
        }

        public String getSummary()
        {
            return "mock";
        }
    }
}