package com.zutubi.pulse;

import com.zutubi.prototype.config.MockConfigurationProvider;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.DefaultAgent;
import com.zutubi.pulse.agent.Status;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SimpleMasterConfigurationManager;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.DelegateScmClientFactory;
import com.zutubi.pulse.core.scm.MockScmClient;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.events.AgentRemovedEvent;
import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.scm.ScmChangeEvent;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeStatus;
import junit.framework.TestCase;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    private MockAgentManager agentManager;
    private AgentConfiguration slave1000;
    private AgentConfiguration slave2000;
    private AgentConfiguration slave3000;
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
        slave1000 = createAgentConfig(1000);
        agentManager.addAgent(slave1000);
        slave2000 = createAgentConfig(2000);
        agentManager.addAgent(slave2000);
        slave3000 = createAgentConfig(3000);
        agentManager.addAgent(slave3000);

        MasterConfigurationManager configurationManager = new SimpleMasterConfigurationManager();
        MockConfigurationProvider configurationProvider = new MockConfigurationProvider();
        configurationProvider.insert("test", new GeneralAdminConfiguration());

        queue = new ThreadedRecipeQueue();
        queue.setEventManager(eventManager);
        queue.setAgentManager(agentManager);
        queue.setUnsatisfiableTimeout(-1);
        queue.setConfigurationManager(configurationManager);
        queue.setConfigurationProvider(configurationProvider);
        queue.setScmClientFactory(new DelegateScmClientFactory()
        {
            public ScmClient createClient(Configuration config) throws ScmException
            {
                MockScm scm = (MockScm) config;
                return new MockScmClient(scm.throwError);
            }
        });
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
        MockAgentService service = (MockAgentService) agent.getService();

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
        request.getRevision().update(createRevision(88), getPulseFileForRevision(88));
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

        queue.handleEvent(new ScmChangeEvent(projectConfig, createRevision(98), createRevision(1)));
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

        queue.handleEvent(new ScmChangeEvent(createProjectConfig(), createRevision(98), createRevision(1)));
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
        CustomTypeConfiguration type = new CustomTypeConfiguration()
        {
            public String getPulseFile(long id, ProjectConfiguration projectConfig, Revision revision, PatchArchive patch)
            {
                long number = Long.valueOf(revision.getRevisionString());
                if (number == 0)
                {
                    throw new BuildException("test");
                }
                return getPulseFileForRevision(number);
            }
        };

        projectConfig.setType(type);
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
        request.getRevision().update(createRevision(5), getPulseFileForRevision(5));
        request.getRevision().apply(request.getRequest());
        queue.enqueue(request);

        queue.handleEvent(new ScmChangeEvent(projectConfig, createRevision(98), createRevision(1)));
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
        queue.handleEvent(new ScmChangeEvent(projectConfig, createRevision(-1), createRevision(1)));
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

        queue.handleEvent(new ScmChangeEvent(projectConfig, createRevision(0), createRevision(1)));
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
        queue.handleEvent(new ScmChangeEvent(projectConfig, createRevision(-1), createRevision(1)));
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

    private void sendOfflineEvent(AgentConfiguration agentConfig)
    {
        Agent a = agentManager.getAgent(agentConfig.getHandle());
        a.updateStatus(new SlaveStatus(Status.OFFLINE, "oops"));
        AgentRemovedEvent event = new AgentRemovedEvent(this, a);
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

    private AgentConfiguration createAgentConfig(long handle)
    {
        AgentConfiguration result = new AgentConfiguration();
        result.setHandle(handle);
        result.setName("name" + handle);
        result.setHost("host" + handle);
        return result;
    }

    private Agent createAvailableAgent(long type)
    {
        DefaultAgent slaveAgent = new DefaultAgent(createAgentConfig(type), new AgentState(), new MockAgentService(type));
        slaveAgent.updateStatus(new SlaveStatus(Status.IDLE, 0, false));
        return slaveAgent;
    }

    private RecipeDispatchRequest createDispatchRequest(int type, long id)
    {
        return createDispatchRequest(type, id, createProjectConfig());
    }

    private RecipeDispatchRequest createDispatchRequest(int type, long id, ProjectConfiguration projectConfig)
    {
        Project project = new Project();
        project.setConfig(projectConfig);
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, 100, false);
        BuildHostRequirements requirements = new MockBuildHostRequirements(type);
        RecipeRequest request = new RecipeRequest("project", id, null, null, null, false, false, false, null, new LinkedList<ResourceProperty>());
        request.setBootstrapper(new ChainBootstrapper());
        return new RecipeDispatchRequest(project, requirements, new BuildRevision(), request, result);
    }

    private RecipeDispatchRequest createDispatchRequest(int type)
    {
        return createDispatchRequest(type, -1);
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

        public String getType()
        {
            return "mock";
        }

        public String getPreviousRevision(String revision)
        {
            return null;
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

        public Agent getAgent(long handle)
        {
            return onlineAgents.get(handle);
        }

        public void pingAgent(long handle)
        {
            throw new RuntimeException("Method not implemented.");
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

        public void addAgent(AgentConfiguration agentConfig)
        {
            DefaultAgent agent = new DefaultAgent(agentConfig, new AgentState(), new MockAgentService(agentConfig.getHandle()));
            agent.updateStatus(new SlaveStatus(Status.IDLE, 0, true));
            onlineAgents.put(agentConfig.getHandle(), agent);
        }

        public void enableAgent(long handle)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void disableAgent(long handle)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void setAgentState(long handle, AgentState.EnableState state)
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

        public List<Resource> discoverResources()
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

        public void collectResults(String project, long recipeId, boolean incremental, File outputDest, File workDest)
        {
        }

        public void cleanup(String project, long recipeId, boolean incremental)
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

        public boolean fulfilledBy(RecipeDispatchRequest request, AgentService service)
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
        return new Revision(null, null, null, Long.toString(rev));
    }
}