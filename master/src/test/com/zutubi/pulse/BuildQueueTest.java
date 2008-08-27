package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.model.BuildReason;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.security.AccessManager;
import static org.mockito.Mockito.mock;

/**
 */
public class BuildQueueTest extends PulseTestCase
{
    private BuildQueue queue;

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

        AccessManager mockManager = mock(AccessManager.class);

        queue = new BuildQueue();
        queue.setAccessManager(mockManager);
    }

    protected void tearDown() throws Exception
    {
        queue = null;
        p1 = null;
        p2 = null;
        pc1 = null;
        pc2 = null;

        super.tearDown();
    }

    public void testSimpleQueue()
    {
        assertTrue(queue.buildRequested(createRequest(p1)));
        assertNull(queue.buildCompleted(p1));
    }

    public void testQueueTwice()
    {
        assertTrue(queue.buildRequested(createRequest(p1)));
        assertNull(queue.buildCompleted(p1));
        assertTrue(queue.buildRequested(createRequest(p1)));
        assertNull(queue.buildCompleted(p1));
    }

    public void testMultiProjects()
    {
        assertTrue(queue.buildRequested(createRequest(p1)));
        assertTrue(queue.buildRequested(createRequest(p2)));
        assertNull(queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p2));
    }

    public void testCancelQueuedBuild()
    {
        assertTrue(queue.buildRequested(createRequest(p1)));
        BuildRequestEvent request2 = createRequest(p1);
        assertFalse(queue.buildRequested(request2));
        queue.cancelBuild(request2.getId());
        assertNull(queue.buildCompleted(p1));
    }

    public void testCancelLastBuildInQueue()
    {
        BuildRequestEvent event1 = createFixedRequest(p1);
        BuildRequestEvent event2 = createFixedRequest(p1);
        assertTrue(queue.buildRequested(event1));
        assertFalse(queue.buildRequested(event2));
        assertTrue(queue.cancelBuild(event2.getId()));
        assertNull(queue.buildCompleted(p1));
    }

    public void testCancelFirstBuildInQueue()
    {
        // can not cancel the first build in the queue, since this build will be active.
        BuildRequestEvent event1 = createFixedRequest(p1);
        BuildRequestEvent event2 = createFixedRequest(p1);
        assertTrue(queue.buildRequested(event1));
        assertFalse(queue.buildRequested(event2));
        assertFalse(queue.cancelBuild(event1.getId()));
        assertNotNull(queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    public void testCancelMiddleBuildInQueue()
    {
        BuildRequestEvent event1 = createFixedRequest(p1);
        BuildRequestEvent event2 = createFixedRequest(p1);
        BuildRequestEvent event3 = createFixedRequest(p1);
        assertTrue(queue.buildRequested(event1));
        assertFalse(queue.buildRequested(event2));
        assertFalse(queue.buildRequested(event3));
        assertTrue(queue.cancelBuild(event2.getId()));
        assertNotNull(queue.buildCompleted(p1));
        assertNull(queue.buildCompleted(p1));
    }

    private BuildRequestEvent createRequest(Project project)
    {
        return new BuildRequestEvent(this, new MockBuildReason(), project, new BuildRevision());
    }

    private BuildRequestEvent createFixedRequest(Project project)
    {
        return new BuildRequestEvent(this, new MockBuildReason(), project, new BuildRevision(new Revision(0), "", false));
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
