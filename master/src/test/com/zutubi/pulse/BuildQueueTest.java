package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.PersistentName;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.model.BuildReason;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class BuildQueueTest extends PulseTestCase
{
    private BuildQueue queue = new BuildQueue();
    private Project p1;
    private Project p2;

    protected void setUp() throws Exception
    {
        super.setUp();

        p1 = new Project();
        p2 = new Project();
    }

    public void testSimpleQueue()
    {
        assertTrue(queue.buildRequested(createEvent(p1, "spec1", 1)));
        assertNull(queue.buildCompleted(p1));
    }

    public void testQueueTwice()
    {
        assertTrue(queue.buildRequested(createEvent(p1, "spec1", 1)));
        assertNull(queue.buildCompleted(p1));
        assertTrue(queue.buildRequested(createEvent(p1, "spec1", 1)));
        assertNull(queue.buildCompleted(p1));
    }

    public void testSimpleWait()
    {
        assertTrue(queue.buildRequested(createEvent(p1, "spec1", 1)));
        BuildRequestEvent request2 = createEvent(p1, "spec2", 2);
        assertFalse(queue.buildRequested(request2));
        assertEquals(request2, queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    public void testWaitSameSpec()
    {
        assertTrue(queue.buildRequested(createEvent(p1, "spec1", 1)));
        BuildRequestEvent request2 = createEvent(p1, "spec1", 1);
        request2.getRevision().apply(new RecipeRequest("project", "spec", 1, null, false));
        assertFalse(queue.buildRequested(request2));
        assertEquals(request2, queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    public void testMultiProjects()
    {
        assertTrue(queue.buildRequested(createEvent(p1, "spec1-1", 11)));
        assertTrue(queue.buildRequested(createEvent(p2, "spec2-1", 21)));
        assertNull(queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p2));
    }

    public void testQueueMultipleSpecs()
    {
        assertTrue(queue.buildRequested(createEvent(p1, "spec1", 1)));
        BuildRequestEvent request2 = createEvent(p1, "spec2", 2);
        assertFalse(queue.buildRequested(request2));
        BuildRequestEvent request3 = createEvent(p1, "spec3", 3);
        assertFalse(queue.buildRequested(request2));
        assertFalse(queue.buildRequested(request3));
        assertEquals(request2, queue.buildCompleted(p1));
        assertEquals(request3, queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    public void testQueueSameSpecTwice()
    {
        assertTrue(queue.buildRequested(createEvent(p1, "spec1", 1)));
        BuildRequestEvent request2 = createEvent(p1, "spec2", 2);
        assertFalse(queue.buildRequested(request2));
        BuildRequestEvent request3 = createEvent(p1, "spec2", 2);
        assertFalse(queue.buildRequested(request2));
        assertFalse(queue.buildRequested(request3));
        assertEquals(request2, queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    public void testCancelQueuedBuild()
    {
        assertTrue(queue.buildRequested(createEvent(p1, "spec1", 1)));
        BuildRequestEvent request2 = createEvent(p1, "spec2", 2);
        assertFalse(queue.buildRequested(request2));
        queue.cancelBuild(request2.getId());
        assertNull(queue.buildCompleted(p1));
    }

    private BuildRequestEvent createEvent(Project project, String spec, long specId)
    {
        PersistentName specName = new PersistentName(spec);
        specName.setId(specId);
        BuildSpecification specification = new BuildSpecification();
        specification.setId(specId * 100);
        specification.setPname(specName);
        return new BuildRequestEvent(this, new MockBuildReason(), null, project, specification, new BuildRevision());
    }

    private class MockBuildReason implements BuildReason
    {
        public boolean isUser()
        {
            return false;
        }

        public String getSummary()
        {
            return "mock";
        }

        public Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }
    }
}
