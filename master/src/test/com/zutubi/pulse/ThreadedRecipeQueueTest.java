/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.model.BuildHostRequirements;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.SlaveAgent;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * <class-comment/>
 */
public class ThreadedRecipeQueueTest extends TestCase implements EventListener
{
    private ThreadedRecipeQueue queue;
    private Semaphore semaphore;
    private Semaphore eventSemaphore;
    private ThreadedRecipeQueueTest.MockAgentManager agentManager;
    private Slave slave1000;
    private Slave slave2000;
    private Slave slave3000;
    private RecipeErrorEvent recipeError;
    private DefaultEventManager eventManager;

    public ThreadedRecipeQueueTest(String testName)
    {
        super(testName);
        semaphore = new Semaphore(0);
        eventSemaphore = new Semaphore(0);
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
        slave1000 = null;
        slave2000 = null;
        slave3000 = null;
        agentManager = null;
        eventManager = null;
        semaphore = null;
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

    public void testStopStart() throws Exception
    {
        Thread.sleep(100);
        queue.stop(true);
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

        queue.enqueue(createDispatchRequest(1000, 1001));
        assertFalse(semaphore.tryAcquire(2, TimeUnit.SECONDS));

        sendOfflineEvent(slave1000);
        assertTrue(eventSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "Connection to agent lost during recipe execution");

        assertFalse(semaphore.tryAcquire(2, TimeUnit.SECONDS));
        sendOnlineEvent(slave1000);
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testNoOnlineAgent() throws Exception
    {
        queue.setCheckOnEnqueue(true);
        queue.enqueue(createDispatchRequest(0, 1000));
        assertTrue(eventSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertRecipeError(1000, "No online agent is capable of executing the build stage");
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
        return new SlaveAgent(createSlave(type), null, new MockBuildService(type));
    }

    private RecipeDispatchRequest createDispatchRequest(int type, long id)
    {
        BuildHostRequirements requirements = new MockBuildHostRequirements(type);
        RecipeRequest request = new RecipeRequest(id, null);
        request.setBootstrapper(new ChainBootstrapper());
        return new RecipeDispatchRequest(requirements, new LazyPulseFile("howdy :)"), request, null);
    }

    private RecipeDispatchRequest createDispatchRequest(int type)
    {
        return createDispatchRequest(type, -1);
    }

    public void handleEvent(Event evt)
    {
        recipeError = (RecipeErrorEvent) evt;
        eventSemaphore.release();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeErrorEvent.class};
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

        public void newSlave(long id)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void slaveChanged(long id)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void addSlave(Slave slave)
        {
            onlineAgents.put(slave.getId(), new SlaveAgent(slave, null, new MockBuildService(slave.getId())));
        }
    }

    class MockBuildService implements BuildService
    {
        private long type;

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
            return true;
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

        public boolean fulfilledBy(BuildService service)
        {
            return ((MockBuildService) service).getType() == type;
        }

        public String getSummary()
        {
            return "mock";
        }
    }
}