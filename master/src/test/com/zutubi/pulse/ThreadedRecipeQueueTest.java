package com.zutubi.pulse;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.SlaveAgent;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.scm.SCMChangeEvent;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import junit.framework.TestCase;

import java.io.File;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * <class-comment/>
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
    private RecipeErrorEvent recipeError;
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
        queue.setCheckOnEnqueue(false);
        queue.init();
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

        queue.available(createAvailableAgent(0));
        queue.available(createAvailableAgent(1));

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
        queue.available(createAvailableAgent(0));
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
        Thread.yield();

        queue.enqueue(createDispatchRequest(0));
        queue.available(createAvailableAgent(0));

        // Shouldn't dispatch while stopped
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));

        assertEquals(1, queue.length());

        queue.start();
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testIncompatibleService() throws Exception
    {
        queue.enqueue(createDispatchRequest(0));
        queue.available(createAvailableAgent(1));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
    }

    public void testCompatibleAndIncompatibleService() throws Exception
    {
        queue.enqueue(createDispatchRequest(0));
        queue.available(createAvailableAgent(1));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
        queue.available(createAvailableAgent(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testTwoBuildsSameService() throws Exception
    {
        queue.available(createAvailableAgent(0));

        queue.enqueue(createDispatchRequest(0, 1000));
        queue.enqueue(createDispatchRequest(0, 1001));

        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());

        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testRecipeError() throws Exception
    {
        queue.available(createAvailableAgent(0));

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
        queue.available(createAvailableAgent(0));

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
        queue.available(createAvailableAgent(0));
        queue.available(createAvailableAgent(1));
        queue.available(createAvailableAgent(2));

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

        queue.available(createAvailableAgent(0));
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
        queue.available(createAvailableAgent(0));

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
        queue.setCheckOnEnqueue(true);
        queue.enqueue(createDispatchRequest(1000, 1000));
        queue.enqueue(createDispatchRequest(1000, 1001));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        sendOfflineEvent(slave1000);
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "No online agent is capable of executing the build stage");
    }

    public void testNoOnlineAgent() throws Exception
    {
        queue.setCheckOnEnqueue(true);
        queue.enqueue(createDispatchRequest(0, 1000));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "No online agent is capable of executing the build stage");
    }

    public void testAgentRejectsBuild() throws Exception
    {
        Agent agent = createAvailableAgent(0);
        queue.available(agent);
        MockBuildService service = (MockBuildService) agent.getBuildService();

        service.setAcceptBuild(false);
        queue.enqueue(createDispatchRequest(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        // Still not dispatched
        assertEquals(1, queue.length());

        service.setAcceptBuild(true);
        sendOnlineEvent(slave1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        // Now dispatched
        assertEquals(0, queue.length());
    }

    public void testErrorDeterminingRevision() throws Exception
    {
        MockScm scm = new MockScm(true);
        queue.available(createAvailableAgent(0));
        queue.enqueue(createDispatchRequest(0, 1000, scm));

        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "Unable to determine revision to build: test");
    }

    public void testFixedRevision() throws Exception
    {
        RecipeDispatchRequest request = createDispatchRequest(0);
        request.getRevision().update(new NumericalRevision(88), getPulseFileForRevision(88));
        request.getRevision().fix();

        queue.available(createAvailableAgent(0));
        queue.enqueue(request);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(dispatchedEvent.getRequest().getPulseFileSource(), getPulseFileForRevision(88));
    }

    public void testChangeWhileQueued() throws Exception
    {
        MockScm scm = new MockScm();
        queue.enqueue(createDispatchRequest(0, 1000, scm));
        queue.enqueue(createDispatchRequest(0, 1001, scm));

        queue.available(createAvailableAgent(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(98), new NumericalRevision(1)));
        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRequest().getId());
        assertEquals(getPulseFileForRevision(98), dispatchedEvent.getRequest().getPulseFileSource());
    }

    public void testChangeOtherScmWhileQueued() throws Exception
    {
        MockScm scm = new MockScm();
        scm.setId(12);
        queue.enqueue(createDispatchRequest(0, 1000, scm));
        queue.enqueue(createDispatchRequest(0, 1001, scm));

        queue.available(createAvailableAgent(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.handleEvent(new SCMChangeEvent(new MockScm(), new NumericalRevision(98), new NumericalRevision(1)));
        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRecipeId());
        assertEquals(getPulseFileForRevision(1), dispatchedEvent.getRequest().getPulseFileSource());
    }

    public void testChangeButFixed() throws Exception
    {
        queue.available(createAvailableAgent(0));

        MockScm scm = new MockScm();
        queue.enqueue(createDispatchRequest(0, 1000, scm));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        RecipeDispatchRequest request = createDispatchRequest(0, 1001);
        request.getRevision().update(new NumericalRevision(5), getPulseFileForRevision(5));
        request.getRevision().fix();
        queue.enqueue(request);

        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(98), new NumericalRevision(1)));
        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRecipeId());
        assertEquals(getPulseFileForRevision(5), dispatchedEvent.getRequest().getPulseFileSource());
    }

    public void testChangeMakesUnfulfillable() throws Exception
    {
        MockScm scm = new MockScm();
        scm.setId(12);
        queue.enqueue(createDispatchRequest(0, 1000, scm));
        queue.enqueue(createDispatchRequest(0, 1001, scm));

        queue.setCheckOnEnqueue(true);
        Agent agent = createAvailableAgent(0);
        queue.available(agent);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));


        // Negative revision will be rejected by mock
        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(-1), new NumericalRevision(1)));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "No online agent is capable of executing the build stage");
    }

    public void testChangeCantNewPulseFile() throws Exception
    {
        queue.available(createAvailableAgent(0));

        MockScm scm = new MockScm();
        queue.enqueue(createDispatchRequest(0, 1000, scm));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.enqueue(createDispatchRequest(0, 1001, scm));

        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(0), new NumericalRevision(1)));
        sendRecipeCompleted(1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRecipeId());
        assertEquals(getPulseFileForRevision(1), dispatchedEvent.getRequest().getPulseFileSource());
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
        assertNotNull(recipeError);
        assertEquals(id, recipeError.getRecipeId());
        assertEquals(message, recipeError.getErrorMessage());
    }

    private void sendOnlineEvent(Slave slave)
    {
        SlaveAvailableEvent event = new SlaveAvailableEvent(this, slave);
        queue.handleEvent(event);
    }

    private void sendOfflineEvent(Slave slave)
    {
        SlaveAgent a = (SlaveAgent) agentManager.getAgent(slave);
        a.failedPing(0, "oops");
        SlaveUnavailableEvent event = new SlaveUnavailableEvent(this, slave);
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
        slaveAgent.pinged(0, Version.getVersion().getBuildNumberAsInt());
        return slaveAgent;
    }

    private RecipeDispatchRequest createDispatchRequest(int type, long id)
    {
        return createDispatchRequest(type, id, new MockScm());
    }

    private RecipeDispatchRequest createDispatchRequest(int type, long id, Scm scm)
    {
        Project project = new Project("test", "test description", new MockPulseFileDetails());
        project.setScm(scm);
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, "spec", 100);
        BuildHostRequirements requirements = new MockBuildHostRequirements(type);
        RecipeRequest request = new RecipeRequest(id, null);
        request.setBootstrapper(new ChainBootstrapper());
        return new RecipeDispatchRequest(requirements, new BuildRevision(), request, result);
    }

    private RecipeDispatchRequest createDispatchRequest(int type)
    {
        return createDispatchRequest(type, -1);
    }

    public void handleEvent(Event evt)
    {
        if(evt instanceof RecipeErrorEvent)
        {
            recipeError = (RecipeErrorEvent) evt;
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

    class MockScm extends Scm
    {
        private boolean throwError = false;

        public MockScm()
        {
            setId(98765);
        }

        public MockScm(boolean throwError)
        {
            this.throwError = throwError;
        }

        public void setThrowError(boolean throwError)
        {
            this.throwError = throwError;
        }

        public SCMServer createServer() throws SCMException
        {
            return new MockScmServer(throwError);
        }
    }

    class MockScmServer implements SCMServer
    {
        private boolean throwError = false;

        public MockScmServer()
        {
        }

        public MockScmServer(boolean throwError)
        {
            this.throwError = throwError;
        }

        public void setThrowError(boolean throwError)
        {
            this.throwError = throwError;
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

        public Revision checkout(long id, File toDirectory, Revision revision, List<Change> changes) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String checkout(long id, Revision revision, String file) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
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

        public RemoteFile getFile(String path) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public List<RemoteFile> getListing(String path) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void update(File workDir, Revision rev) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean supportsUpdate()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void tag(Revision revision, String name, boolean moveExisting) throws SCMException
        {
            throw new RuntimeException("Method not implemented");
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

        public String getPulseFile(long id, Project project, Revision revision)
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

        public void addSlave(Slave slave)
        {
            SlaveAgent agent = new SlaveAgent(slave, null, null, new MockBuildService(slave.getId()));
            agent.pinged(0, Version.getVersion().getBuildNumberAsInt());
            onlineAgents.put(slave.getId(), agent);
        }
    }

    class MockBuildService implements BuildService
    {
        private long type;
        private boolean acceptBuild = true;

        public MockBuildService(long type)
        {
            this.type = type;
        }

        public boolean hasResource(String resource, String version)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean build(RecipeRequest request)
        {
            semaphore.release();
            return acceptBuild;
        }

        public void collectResults(long recipeId, File outputDest, File workDest)
        {
        }

        public void cleanup(long recipeId)
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