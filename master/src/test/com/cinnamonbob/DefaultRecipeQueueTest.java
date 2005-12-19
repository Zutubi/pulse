package com.cinnamonbob;

import junit.framework.*;
import com.cinnamonbob.model.BuildHostRequirements;
import com.cinnamonbob.model.MasterBuildHostRequirements;
import com.cinnamonbob.core.event.DefaultEventManager;

/**
 * <class-comment/>
 */
public class DefaultRecipeQueueTest extends TestCase
{
    private DefaultRecipeQueue queue;

    public DefaultRecipeQueueTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        queue = new DefaultRecipeQueue();
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
        return new MasterBuildService();
    }

    private RecipeDispatchRequest createDispatchRequest()
    {
        BuildHostRequirements requirements = new MasterBuildHostRequirements();
        RecipeRequest request = new RecipeRequest(-1, null, null, null);
        return new RecipeDispatchRequest(requirements, request);
    }
}