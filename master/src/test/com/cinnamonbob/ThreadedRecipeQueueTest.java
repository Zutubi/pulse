package com.cinnamonbob;

import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.events.DefaultEventManager;
import com.cinnamonbob.events.build.RecipeCompletedEvent;
import com.cinnamonbob.events.build.RecipeErrorEvent;
import com.cinnamonbob.model.BuildHostRequirements;
import junit.framework.TestCase;

import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * <class-comment/>
 */
public class ThreadedRecipeQueueTest extends TestCase
{
    private ThreadedRecipeQueue queue;
    private Semaphore semaphore;

    public ThreadedRecipeQueueTest(String testName)
    {
        super(testName);
        semaphore = new Semaphore(0);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        queue = new ThreadedRecipeQueue();
        queue.setEventManager(new DefaultEventManager());
        queue.start();
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

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

        queue.enqueue(createDispatchRequest(0));

        Thread.sleep(100);

        assertEquals(2, queue.length());
        assertTrue(queue.isRunning());
        assertFalse(queue.isStopped());

        queue.available(createAvailableService(0));
        queue.available(createAvailableService(0));

        // If it takes longer than 30 seconds, something is wrong
        // Usually this will be pretty immediate.
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        Thread.sleep(100);
        assertEquals(0, queue.length());

    }

    public void testStopStart() throws Exception
    {
        Thread.yield();
        queue.stop();
        Thread.yield();

        queue.enqueue(createDispatchRequest(0));
        queue.available(createAvailableService(0));

        // Shouldn't dispatch while stopped
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));

        assertEquals(1, queue.length());

        queue.start();
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testIncompatibleService() throws Exception
    {
        queue.enqueue(createDispatchRequest(0));
        queue.available(createAvailableService(1));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
    }

    public void testCompatibleAndIncompatibleService() throws Exception
    {
        queue.enqueue(createDispatchRequest(0));
        queue.available(createAvailableService(1));
        assertFalse(semaphore.tryAcquire(100, TimeUnit.MILLISECONDS));
        assertEquals(1, queue.length());
        queue.available(createAvailableService(0));
        assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));
    }

    public void testTwoBuildsSameService() throws Exception
    {
        BuildService service = createAvailableService(0);
        queue.available(service);

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
        BuildService service = createAvailableService(0);
        queue.available(service);

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
        BuildService service = createAvailableService(0);
        queue.available(service);

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
        BuildService service0 = createAvailableService(0);
        BuildService service1 = createAvailableService(1);
        BuildService service2 = createAvailableService(2);

        queue.available(service0);
        queue.available(service1);
        queue.available(service2);

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

    private BuildService createAvailableService(int type)
    {
        return new MockBuildService(type);
    }

    private RecipeDispatchRequest createDispatchRequest(int type, long id)
    {
        BuildHostRequirements requirements = new MockBuildHostRequirements(type);
        RecipeRequest request = new RecipeRequest(id, null);
        return new RecipeDispatchRequest(requirements, request);
    }

    private RecipeDispatchRequest createDispatchRequest(int type)
    {
        return createDispatchRequest(type, -1);
    }

    class MockBuildService implements BuildService
    {
        private int type;

        public MockBuildService(int type)
        {
            this.type = type;
        }

        public void build(RecipeRequest request)
        {
            semaphore.release();
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

        public int getType()
        {
            return type;
        }
    }

    class MockBuildHostRequirements implements BuildHostRequirements
    {
        private int type;

        public MockBuildHostRequirements(int type)
        {
            this.type = type;
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