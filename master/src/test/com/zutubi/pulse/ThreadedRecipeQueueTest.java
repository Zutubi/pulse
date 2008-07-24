package com.zutubi.pulse;

import com.zutubi.pulse.agent.*;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.scm.*;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeStatus;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
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
    private MockAgentManager agentManager;
    private SlaveAgent slave1000;
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

        queue = new ThreadedRecipeQueue();
        queue.setEventManager(eventManager);
        queue.setAgentManager(agentManager);
        queue.setMasterLocationProvider(new MasterLocationProvider()
        {
            public String getMasterLocation()
            {
                return "test";
            }

            public String getMasterUrl()
            {
                return "test";
            }
        });
        queue.setUnsatisfiableTimeout(-1);
        queue.init();

        slave1000 = createAvailableAgent(1000);
        createAvailableAgent(2000);

        recipeErrors = new LinkedList<RecipeErrorEvent>();
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        eventManager.unregister(this);
        slave1000 = null;
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

        createAvailableAgent(0);
        createAvailableAgent(1);

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
        createAvailableAgent(0);
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
        createAvailableAgent(0);

        // Shouldn't dispatch while stopped
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));

        assertEquals(1, queue.length());

        queue.start();
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testIncompatibleService() throws Exception
    {
        queue.enqueue(createDispatchRequest(0));
        createAvailableAgent(1);
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
    }

    public void testCompatibleAndIncompatibleService() throws Exception
    {
        queue.enqueue(createDispatchRequest(0));
        createAvailableAgent(1);
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
        createAvailableAgent(0);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testTwoBuildsSameService() throws Exception
    {
        SlaveAgent agent = createAvailableAgent(0);

        queue.enqueue(createDispatchRequest(0, 1000));
        queue.enqueue(createDispatchRequest(0, 1001));

        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());

        sendAvailable(agent);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testTwoBuildsSameServiceOnlineAfter() throws Exception
    {
        SlaveAgent agent = createAvailableAgent(0);
        queue.enqueue(createDispatchRequest(0, 1000));
        queue.enqueue(createDispatchRequest(0, 1001));
        queue.handleEvent(new AgentOnlineEvent(this, agent));

        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(semaphore.tryAcquire(1, TimeUnit.SECONDS));
        assertEquals(1, queue.length());

        sendAvailable(agent);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testThreeServices() throws Exception
    {
        createAvailableAgent(0);
        SlaveAgent agent1 = createAvailableAgent(1);
        createAvailableAgent(2);

        queue.enqueue(createDispatchRequest(0, 1000));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.enqueue(createDispatchRequest(1, 1001));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.enqueue(createDispatchRequest(1, 1002));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));

        queue.enqueue(createDispatchRequest(2, 1003));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        sendAvailable(agent1);
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

        createAvailableAgent(0);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        Thread.sleep(500);

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
        createAvailableAgent(0);

        queue.enqueue(createDispatchRequest(0, 1000));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertFalse(queue.cancelRequest(1000));
    }

    public void testOfflineWhileQueued() throws Exception
    {
        queue.setUnsatisfiableTimeout(0);
        queue.enqueue(createDispatchRequest(1000, 1000));
        queue.enqueue(createDispatchRequest(1000, 1001));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        takeOffline(slave1000);
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

    public void testAgentErrorOnBuild() throws Exception
    {
        SlaveAgent agent = createAvailableAgent(0);
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
        sendAvailable(agent);
        queue.enqueue(createDispatchRequest(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(0, queue.length());
    }

    public void testErrorDeterminingRevision() throws Exception
    {
        MockScm scm = new MockScm(true);
        createAvailableAgent(0);
        queue.enqueue(createDispatchRequest(0, 1000, scm));

        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "Unable to determine revision to build: test");
    }

    public void testFixedRevision() throws Exception
    {
        RecipeDispatchRequest request = createDispatchRequest(0);
        request.getRevision().update(new NumericalRevision(88), getPulseFileForRevision(88));
        request.getRevision().apply(request.getRequest());

        createAvailableAgent(0);
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

        SlaveAgent agent = createAvailableAgent(0);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(98), new NumericalRevision(1)));
        sendAvailable(agent);
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

        SlaveAgent agent = createAvailableAgent(0);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.handleEvent(new SCMChangeEvent(new MockScm(), new NumericalRevision(98), new NumericalRevision(1)));
        sendAvailable(agent);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRecipeId());
        assertEquals(getPulseFileForRevision(1), dispatchedEvent.getRequest().getPulseFileSource());
    }

    public void testChangeButFixed() throws Exception
    {
        SlaveAgent agent = createAvailableAgent(0);

        MockScm scm = new MockScm();
        queue.enqueue(createDispatchRequest(0, 1000, scm));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        RecipeDispatchRequest request = createDispatchRequest(0, 1001);
        request.getRevision().update(new NumericalRevision(5), getPulseFileForRevision(5));
        request.getRevision().apply(request.getRequest());
        queue.enqueue(request);

        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(98), new NumericalRevision(1)));
        sendAvailable(agent);
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

        queue.setUnsatisfiableTimeout(0);
        createAvailableAgent(0);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        // Negative revision will be rejected by mock
        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(-1), new NumericalRevision(1)));
        assertTrue(errorSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1001, "No online agent is capable of executing the build stage");
    }

    public void testChangeCantNewPulseFile() throws Exception
    {
        SlaveAgent agent = createAvailableAgent(0);
        MockScm scm = new MockScm();
        queue.enqueue(createDispatchRequest(0, 1000, scm));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));

        queue.enqueue(createDispatchRequest(0, 1001, scm));

        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(0), new NumericalRevision(1)));
        sendAvailable(agent);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(dispatchedSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(1001, dispatchedEvent.getRecipeId());
        assertEquals(getPulseFileForRevision(1), dispatchedEvent.getRequest().getPulseFileSource());
    }

    public void testOfflineRemovedFromAvailable() throws InterruptedException
    {
        SlaveAgent offAgent = createAvailableAgent(0);
        agentManager.unavailable(offAgent);
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

        takeOffline(slave1000);
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

        takeOffline(slave1000);
        assertFalse(errorSemaphore.tryAcquire(3, TimeUnit.SECONDS));
        assertNoError(1001);
    }

    public void testChangeMakesTimeout() throws Exception
    {
        MockScm scm = new MockScm();
        scm.setId(12);
        queue.enqueue(createDispatchRequest(0, 1000, scm));
        queue.enqueue(createDispatchRequest(0, 1001, scm));

        queue.setSleepInterval(1);
        queue.setUnsatisfiableTimeout(1);
        createAvailableAgent(0);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        // Negative revision will be rejected by mock
        queue.handleEvent(new SCMChangeEvent(scm, new NumericalRevision(-1), new NumericalRevision(1)));
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

    private void takeOffline(SlaveAgent agent)
    {
        agent.updateStatus(new SlaveStatus(PingStatus.OFFLINE, "oops"));
        agentManager.offline(agent);
        queue.handleEvent(new AgentOfflineEvent(this, agent));
    }

    private void sendAvailable(SlaveAgent agent)
    {
        agentManager.available(agent);
        queue.handleEvent(new AgentAvailableEvent(this, agent));
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

    private SlaveAgent createAvailableAgent(long type)
    {
        SlaveAgent slaveAgent = new SlaveAgent(createSlave(type), null, null, new MockBuildService(type));
        slaveAgent.updateStatus(new SlaveStatus(PingStatus.IDLE, 0, false));
        agentManager.addAgent(slaveAgent);
        agentManager.online(slaveAgent);
        queue.handleEvent(new AgentOnlineEvent(this, slaveAgent));
        agentManager.available(slaveAgent);
        queue.handleEvent(new AgentAvailableEvent(this, slaveAgent));
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
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, new BuildSpecification("spec"), 100, false);
        BuildHostRequirements requirements = new MockBuildHostRequirements(type);
        RecipeRequest request = new RecipeRequest("project", "spec", id, null, null, null, false, false, false, null, new LinkedList<ResourceProperty>());
        request.setBootstrapper(new ChainBootstrapper());
        return new RecipeDispatchRequest(new BuildSpecification("test"), requirements, new BuildRevision(), request, result);
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
            agentManager.unavailable((SlaveAgent) dispatchedEvent.getAgent());
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

        public String getType()
        {
            return "mock";
        }

        public Map<String, String> getRepositoryProperties()
        {
            throw new RuntimeException("Method not implemented.");
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

        public Revision checkout(String id, File toDirectory, Revision revision, SCMCheckoutEventHandler handler) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String checkout(Revision revision, String file) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
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

        public RemoteFile getFile(String path) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public List<RemoteFile> getListing(String path) throws SCMException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void update(String id, File workDir, Revision rev, SCMCheckoutEventHandler handler) throws SCMException
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

        public List<ResourceProperty> getConnectionProperties(String id, File dir) throws SCMException
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void writeConnectionDetails(File outputDir) throws SCMException, IOException
        {
            throw new RuntimeException("Method not implemented.");
        }

        public FileStatus.EOLStyle getEOLPolicy() throws SCMException
        {
            return FileStatus.EOLStyle.BINARY;
        }

        public FileRevision getFileRevision(String path, Revision repoRevision)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public Revision getRevision(String revision) throws SCMException
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void close()
        {
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

        public String getPulseFile(long id, Project project, Revision revision, PatchArchive patch)
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
        private Map<Long, SlaveAgent> slaveAgents = new TreeMap<Long, SlaveAgent>();
        private List<Agent> onlineAgents = new LinkedList<Agent>();
        private List<Agent> availableAgents = new LinkedList<Agent>();

        public List<Agent> getAllAgents()
        {
            return new LinkedList<Agent>(slaveAgents.values());
        }

        public List<Agent> getOnlineAgents()
        {
            return onlineAgents;
        }

        public List<Agent> getAvailableAgents()
        {
            return availableAgents;
        }

        public Agent getAgent(Slave slave)
        {
            return slaveAgents.get(slave.getId());
        }

        public void addAgent(SlaveAgent agent)
        {
            slaveAgents.put(agent.getId(), agent);
        }

        public void online(SlaveAgent agent)
        {
            if(!onlineAgents.contains(agent))
            {
                onlineAgents.add(agent);
            }
        }

        public void offline(SlaveAgent agent)
        {
            onlineAgents.remove(agent);
        }

        public void available(SlaveAgent agent)
        {
            if(!availableAgents.contains(agent))
            {
                availableAgents.add(agent);
            }
        }

        public void unavailable(SlaveAgent agent)
        {
            availableAgents.remove(agent);
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

        public void addSlave(Slave slave)
        {
            SlaveAgent agent = new SlaveAgent(slave, null, null, new MockBuildService(slave.getId()));
            slaveAgents.put(slave.getId(), agent);
        }

        public void setEnableState(Agent agent, Slave.EnableState state)
        {
            throw new RuntimeException("Not implemented");
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