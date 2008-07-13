package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.model.BuildReason;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;

/**
 */
public class BuildQueueTest extends PulseTestCase
{
    private BuildQueue queue = new BuildQueue();

    private Project p1;
    private ProjectConfiguration pc1;
    private Project p2;
    private ProjectConfiguration pc2;

    protected void setUp() throws Exception
    {
        super.setUp();

        p1 = new Project();
        p1.setId(1);
        pc1 = new ProjectConfiguration();
        pc1.setHandle(102);
        p1.setConfig(pc1);

        p2 = new Project();
        p2.setId(2);
        pc2 = new ProjectConfiguration();
        pc2.setHandle(102);
        p2.setConfig(pc2);
    }

    public void testSimpleQueue()
    {
        assertTrue(queue.buildRequested(createEvent(p1)));
        assertNull(queue.buildCompleted(p1));
    }

    public void testQueueTwice()
    {
        assertTrue(queue.buildRequested(createEvent(p1)));
        assertNull(queue.buildCompleted(p1));
        assertTrue(queue.buildRequested(createEvent(p1)));
        assertNull(queue.buildCompleted(p1));
    }

    public void testMultiProjects()
    {
        assertTrue(queue.buildRequested(createEvent(p1)));
        assertTrue(queue.buildRequested(createEvent(p2)));
        assertNull(queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p2));
    }

    public void testCancelQueuedBuild()
    {
        assertTrue(queue.buildRequested(createEvent(p1)));
        BuildRequestEvent request2 = createEvent(p1);
        assertFalse(queue.buildRequested(request2));
        queue.cancelBuild(request2.getId());
        assertNull(queue.buildCompleted(p1));
    }

    private BuildRequestEvent createEvent(Project project)
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
