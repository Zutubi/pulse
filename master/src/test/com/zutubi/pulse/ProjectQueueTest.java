package com.cinnamonbob;

import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.test.BobTestCase;

/**
 */
public class ProjectQueueTest extends BobTestCase
{
    private ProjectQueue queue = new ProjectQueue();
    private Project p1;
    private Project p2;

    protected void setUp() throws Exception
    {
        super.setUp();

        p1 = new Project("p1", "test project 1");
        p2 = new Project("p2", "test project 2");
    }

    public void testSimpleQueue()
    {
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p1, "spec1")));
        assertNull(queue.buildCompleted(p1));
    }

    public void testQueueTwice()
    {
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p1, "spec1")));
        assertNull(queue.buildCompleted(p1));
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p1, "spec1")));
        assertNull(queue.buildCompleted(p1));
    }

    public void testSimpleWait()
    {
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p1, "spec1")));
        BuildRequestEvent request2 = new BuildRequestEvent(this, p1, "spec2");
        assertFalse(queue.buildRequested(request2));
        assertEquals(request2, queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    public void testWaitSameSpec()
    {
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p1, "spec1")));
        BuildRequestEvent request2 = new BuildRequestEvent(this, p1, "spec1");
        assertFalse(queue.buildRequested(request2));
        assertEquals(request2, queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    public void testMultiProjects()
    {
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p1, "spec1-1")));
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p2, "spec2-1")));
        assertNull(queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p2));
    }

    public void testQueueMultipleSpecs()
    {
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p1, "spec1")));
        BuildRequestEvent request2 = new BuildRequestEvent(this, p1, "spec2");
        assertFalse(queue.buildRequested(request2));
        BuildRequestEvent request3 = new BuildRequestEvent(this, p1, "spec3");
        assertFalse(queue.buildRequested(request2));
        assertFalse(queue.buildRequested(request3));
        assertEquals(request2, queue.buildCompleted(p1));
        assertEquals(request3, queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    public void testQueueSameSpecTwice()
    {
        assertTrue(queue.buildRequested(new BuildRequestEvent(this, p1, "spec1")));
        BuildRequestEvent request2 = new BuildRequestEvent(this, p1, "spec2");
        assertFalse(queue.buildRequested(request2));
        BuildRequestEvent request3 = new BuildRequestEvent(this, p1, "spec2");
        assertFalse(queue.buildRequested(request2));
        assertFalse(queue.buildRequested(request3));
        assertEquals(request2, queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }
}
