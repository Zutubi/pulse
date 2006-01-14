package com.cinnamonbob;

import com.cinnamonbob.core.event.DefaultEventManager;
import com.cinnamonbob.model.BuildHostRequirements;
import junit.framework.TestCase;

import java.io.File;

/**
 * <class-comment/>
 */
public class DefaultRecipeQueueTest extends TestCase
{
    private ThreadedRecipeQueue queue;

    public DefaultRecipeQueueTest(String testName)
    {
        super(testName);
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
        queue.enqueue(createDispatchRequest());
        assertEquals(1, queue.length());

        Thread.yield();

        assertEquals(1, queue.length());
        assertTrue(queue.isRunning());
        assertFalse(queue.isStopped());

        queue.enqueue(createDispatchRequest());

        Thread.yield();

        assertEquals(2, queue.length());
        assertTrue(queue.isRunning());
        assertFalse(queue.isStopped());

        queue.available(createAvailableService());
        queue.available(createAvailableService());

        Thread.yield();

        assertEquals(0, queue.length());

    }

    public void testStopStart() throws Exception
    {
        Thread.yield();
        queue.stop();
        Thread.yield();

        queue.enqueue(createDispatchRequest());
        queue.available(createAvailableService());

        Thread.yield();

        assertEquals(1, queue.length());

        queue.start();
        Thread.sleep(100);

        assertEquals(0, queue.length());
    }


    private BuildService createAvailableService()
    {
        return new MockBuildService();
    }

    private RecipeDispatchRequest createDispatchRequest()
    {
        BuildHostRequirements requirements = new MockBuildHostRequirements();
        RecipeRequest request = new RecipeRequest(-1, null);
        return new RecipeDispatchRequest(requirements, request);
    }

    class MockBuildService implements BuildService
    {

        public void build(RecipeRequest request)
        {
        }

        public void collectResults(long recipeId, File dir)
        {
        }

        public void cleanup(long recipeId)
        {
        }

        public String getHostName()
        {
            return "[mock]";
        }

        public String getUrl()
        {
            return null;
        }
    }

    class MockBuildHostRequirements implements BuildHostRequirements
    {

        public boolean fulfilledBy(BuildService service)
        {
            return true;
        }

        public String getSummary()
        {
            return "mock";
        }
    }
}