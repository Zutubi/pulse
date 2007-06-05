package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.model.BuildReason;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class BuildQueueTest extends PulseTestCase
{
    private BuildQueue queue = new BuildQueue();

    private ProjectConfiguration pc1;
    private ProjectConfiguration pc2;

    protected void setUp() throws Exception
    {
        super.setUp();

        pc1 = new ProjectConfiguration();
        pc1.setHandle(1);
        pc2 = new ProjectConfiguration();
        pc2.setHandle(2);
    }

    public void testSimpleQueue()
    {
        assertTrue(queue.buildRequested(createEvent(pc1)));
        assertNull(queue.buildCompleted(pc1));
    }

    public void testQueueTwice()
    {
        assertTrue(queue.buildRequested(createEvent(pc1)));
        assertNull(queue.buildCompleted(pc1));
        assertTrue(queue.buildRequested(createEvent(pc1)));
        assertNull(queue.buildCompleted(pc1));
    }

    public void testMultiProjects()
    {
        assertTrue(queue.buildRequested(createEvent(pc1)));
        assertTrue(queue.buildRequested(createEvent(pc2)));
        assertNull(queue.buildCompleted(pc1));
        assertNull(queue.buildCompleted(pc2));
    }

    public void testCancelQueuedBuild()
    {
        assertTrue(queue.buildRequested(createEvent(pc1)));
        BuildRequestEvent request2 = createEvent(pc1);
        assertFalse(queue.buildRequested(request2));
        queue.cancelBuild(request2.getId());
        assertNull(queue.buildCompleted(pc1));
    }

    private BuildRequestEvent createEvent(ProjectConfiguration project)
    {
        return new BuildRequestEvent(this, new MockBuildReason(), project, new BuildRevision());
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
