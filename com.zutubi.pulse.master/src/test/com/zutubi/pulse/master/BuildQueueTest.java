package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BuildQueueTest extends BuildQueueTestCase
{
    private BuildQueue queue;

    private Project project1;
    private Project project2;

    protected void setUp() throws Exception
    {
        super.setUp();

        project1 = createProject("project1");
        project2 = createProject("project2");

        queue = new BuildQueue();
        queue.setObjectFactory(objectFactory);

        objectFactory.initProperties(this);
    }

    public void testSimpleQueue()
    {
        final int BUILD_ID = nextId.getAndIncrement();

        final AbstractBuildRequestEvent request = createRequest(project1, BUILD_ID, "source", false, null);

        queue.buildRequested(request);
        assertActive(project1, request);
        assertActive(project2);

        queue.buildCompleted(project1, BUILD_ID);
        assertActive(project1);
        assertActive(project2);
    }

    public void testQueueTwice()
    {
        final int BUILD_ID1 = nextId.getAndIncrement();
        final int BUILD_ID2 = nextId.getAndIncrement();

        final AbstractBuildRequestEvent request1 = createRequest(project1, BUILD_ID1, "source", false, null);
        final AbstractBuildRequestEvent request2 = createRequest(project1, BUILD_ID2, "source", false, null);

        queue.buildRequested(request1);
        assertActive(project1, request1);

        queue.buildCompleted(project1, BUILD_ID1);
        assertActive(project1);

        queue.buildRequested(request2);
        assertActive(project1, request2);

        queue.buildCompleted(project1, BUILD_ID2);
        assertActive(project1);
    }

    public void testQueueBehind()
    {
        final int BUILD_ID1 = nextId.getAndIncrement();
        final int BUILD_ID2 = nextId.getAndIncrement();

        final AbstractBuildRequestEvent request1 = createRequest(project1, BUILD_ID1, "source", false, null);
        final AbstractBuildRequestEvent request2 = createRequest(project1, BUILD_ID2, "source", false, null);

        queue.buildRequested(request1);
        queue.buildRequested(request2);
        assertActive(project1, request1);
        assertQueued(project1, request2);

        queue.buildCompleted(project1, BUILD_ID1);
        assertActive(project1, request2);
        assertQueued(project1);

        queue.buildCompleted(project1, BUILD_ID2);
        assertActive(project1);
        assertQueued(project1);
    }

    public void testMultiProjects()
    {
        final int BUILD_ID1 = nextId.getAndIncrement();
        final int BUILD_ID2 = nextId.getAndIncrement();

        final AbstractBuildRequestEvent request1 = createRequest(project1, BUILD_ID1, "source", false, null);
        final AbstractBuildRequestEvent request2 = createRequest(project2, BUILD_ID2, "source", false, null);

        queue.buildRequested(request1);
        assertActive(project1, request1);
        assertActive(project2);

        queue.buildRequested(request2);
        assertActive(project1, request1);
        assertActive(project2, request2);

        queue.buildCompleted(project1, BUILD_ID1);
        assertActive(project1);
        assertActive(project2, request2);

        queue.buildCompleted(project2, BUILD_ID2);
        assertActive(project1);
        assertActive(project2);
    }

    public void testGetActiveBuildCount()
    {
        final AbstractBuildRequestEvent requestProject1_1 = createRequest(project1, nextId.getAndIncrement(), "source", false, null);
        final AbstractBuildRequestEvent requestProject2_1 = createRequest(project2, nextId.getAndIncrement(), "source", false, null);
        final AbstractBuildRequestEvent requestProject1_2 = createRequest(project1, nextId.getAndIncrement(), "source", false, null);

        queue.buildRequested(requestProject1_1);
        assertEquals(1, queue.getActiveBuildCount());

        queue.buildRequested(requestProject2_1);
        assertEquals(2, queue.getActiveBuildCount());

        queue.buildRequested(requestProject1_2);
        assertEquals(2, queue.getActiveBuildCount());
    }

    public void testCancelActive()
    {
        final AbstractBuildRequestEvent request = createRequest(project1, nextId.getAndIncrement(), "source", false, null);

        queue.buildRequested(request);
        assertActive(project1, request);

        queue.cancelBuild(request.getId());
        assertActive(project1, request);
    }

    public void testCancelFirst()
    {
        cancelHelper(1);
    }

    public void testCancelMiddle()
    {
        cancelHelper(2);
    }

    public void testCancelLast()
    {
        cancelHelper(3);
    }

    private void cancelHelper(final int indexToCancel)
    {
        final AbstractBuildRequestEvent[] requests = new AbstractBuildRequestEvent[] {
                createRequest(project1, nextId.getAndIncrement(), "source", false, null),
                createRequest(project1, nextId.getAndIncrement(), "source", false, null),
                createRequest(project1, nextId.getAndIncrement(), "source", false, null),
                createRequest(project1, nextId.getAndIncrement(), "source", false, null),
        };

        for (AbstractBuildRequestEvent request: requests)
        {
            queue.buildRequested(request);
        }

        // The queue is in reverse order, and does not have the first (active)
        // request.
        final List<AbstractBuildRequestEvent> queuedList = new LinkedList(Arrays.asList(requests));
        queuedList.remove(0);
        Collections.reverse(queuedList);
        final AbstractBuildRequestEvent[] queued = queuedList.toArray(new AbstractBuildRequestEvent[queuedList.size()]);

        assertActive(project1, requests[0]);
        assertQueued(project1, queued);

        queue.cancelBuild(requests[indexToCancel].getId());

        assertActive(project1, requests[0]);
        
        // The queue should be the same, minus the cancelled request.
        assertQueued(project1, CollectionUtils.filterToArray(queued, new Predicate<AbstractBuildRequestEvent>()
        {
            public boolean satisfied(AbstractBuildRequestEvent event)
            {
                return event.getId() != requests[indexToCancel].getId();
            }
        }));
    }

    public void testStop()
    {
        queue.stop();
        queue.buildRequested(createRequest(project1, nextId.getAndIncrement(), "source", false, null));
        assertEquals(0, queue.getActiveBuildCount());
        assertActive(project1);
        assertQueued(project1);
    }

    private void assertActive(Project project, AbstractBuildRequestEvent... events)
    {
        assertActive(queue.takeSnapshot().getActiveBuilds().get(project), events);
    }

    private void assertQueued(Project project, AbstractBuildRequestEvent... events)
    {
        assertQueued(queue.takeSnapshot().getQueuedBuilds().get(project), events);
    }
}
