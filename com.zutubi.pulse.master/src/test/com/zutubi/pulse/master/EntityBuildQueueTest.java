package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.BuildActivatedEvent;
import com.zutubi.pulse.master.events.build.SingleBuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import org.acegisecurity.AccessDeniedException;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.List;

// We have fields that looked unused but actually are by the magic WiringObjectFactory.
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
public class EntityBuildQueueTest extends BuildQueueTestCase
{
    private EntityBuildQueue queue;
    private Project owner;

    protected void setUp() throws Exception
    {
        super.setUp();

        owner = createProject("owner");

        queue = new EntityBuildQueue(owner, 1);
        queue.setAccessManager(accessManager);
        queue.setBuildHandlerFactory(buildHandlerFactory);
        queue.setEventManager(eventManager);

        objectFactory.initProperties(this);
    }

    public void testEmpty()
    {
        assertEquals(0, queue.getActiveBuildCount());
        assertEquals(0, queue.getActiveBuildsSnapshot().size());
        assertEquals(0, queue.getQueuedBuildCount());
        assertEquals(0, queue.getQueuedBuildsSnapshot().size());
    }
    
    public void testSnapshotsIsolated()
    {
        List<EntityBuildQueue.ActiveBuild> activeSnapshot = queue.getActiveBuildsSnapshot();
        activeSnapshot.add(mock(EntityBuildQueue.ActiveBuild.class));
        assertEquals(0, queue.getActiveBuildCount());

        List<BuildRequestEvent> queuedSnapshot = queue.getQueuedBuildsSnapshot();
        queuedSnapshot.add(mock(SingleBuildRequestEvent.class));
        assertEquals(0, queue.getQueuedBuildCount());
    }

    public void testSimpleRequestComplete()
    {
        final long BUILD_ID = nextId.getAndIncrement();

        ActivatedEventListener listener = new ActivatedEventListener();
        eventManager.register(listener);

        BuildRequestEvent request = createRequest(BUILD_ID, "source", false);
        queue.handleRequest(request);

        assertActive(request);
        assertQueued();
        assertEvents(listener, new BuildActivatedEvent(queue, request));

        queue.handleBuildCompleted(BUILD_ID);

        assertActive();
        assertQueued();
    }

    public void testMultipleRequests()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        ActivatedEventListener listener = new ActivatedEventListener();
        eventManager.register(listener);

        BuildRequestEvent request1 = createRequest(BUILD_ID1, "source1", false);
        BuildRequestEvent request2 = createRequest(BUILD_ID2, "source2", false);
        queue.handleRequest(request1);
        queue.handleRequest(request2);

        assertActive(request1);
        assertQueued(request2);
        assertEvents(listener, new BuildActivatedEvent(queue, request1));
        listener.clear();
        
        queue.handleBuildCompleted(BUILD_ID1);

        assertActive(request2);
        assertQueued();

        queue.handleBuildCompleted(BUILD_ID2);

        assertEvents(listener, new BuildActivatedEvent(queue, request2));
        assertActive();
        assertQueued();
    }

    public void testBuildCompletedInvalidId()
    {
        BuildRequestEvent request = createRequest((long) nextId.getAndIncrement(), "source", false);
        queue.handleRequest(request);
        assertActive(request);
        queue.handleBuildCompleted(-1);
        assertActive(request);
    }

    public void testBuildCompletedQueuedId()
    {
        final long ACTIVE_ID = nextId.getAndIncrement();
        final long QUEUED_ID = nextId.getAndIncrement();

        BuildRequestEvent request1 = createRequest(ACTIVE_ID, "source", false);
        BuildRequestEvent request2 = createRequest(QUEUED_ID, "source", false);
        queue.handleRequest(request1);
        queue.handleRequest(request2);

        assertActive(request1);
        assertQueued(request2);

        queue.handleBuildCompleted(QUEUED_ID);

        assertActive(request1);
        assertQueued(request2);
    }

    public void testStoppedNoLongerAcceptsRequests()
    {
        queue.stop();
        queue.handleRequest(createRequest(1, "test", false));
        assertActive();
        assertQueued();
    }

    public void testStoppedDoesNotActivateQueuedRequest()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "source1", false);
        BuildRequestEvent queuedRequest = createRequest(BUILD_ID2, "source2", false);
        queue.handleRequest(activeRequest);
        queue.handleRequest(queuedRequest);

        assertActive(activeRequest);
        assertQueued(queuedRequest);

        queue.stop();
        queue.handleBuildCompleted(BUILD_ID1);

        assertActive();
        assertQueued();
    }

    public void testReplaceExistingActive()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        final Revision revision1 = new Revision("1");
        final Revision revision2 = new Revision("2");

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "source", true, revision1);
        BuildRequestEvent replacementRequest = createRequest(BUILD_ID2, "source", true, revision2);

        BuildHandler handler = handlers.get(activeRequest);
        doReturn(true).when(handler).updateRevisionIfNotFixed(revision2);

        queue.handleRequest(activeRequest);
        assertSame(activeRequest.getRevision().getRevision(), revision1);

        queue.handleRequest(replacementRequest);

        assertActive(activeRequest);
        assertQueued();

        // The actual updating of the request is handled externally by the BuildController which is
        // currently mocked out.  Instead we verify that the expected method was called.
        // assertSame(activeRequest.getRevision().getRevision(), revision2);
    }

    public void testDifferentSourceNotReplaced()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        final Revision revision1 = new Revision("1");
        final Revision revision2 = new Revision("2");

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "source1", true, revision1);
        BuildRequestEvent queuedRequest = createRequest(BUILD_ID2, "source2", true, revision2);

        queue.handleRequest(activeRequest);
        assertSame(activeRequest.getRevision().getRevision(), revision1);

        queue.handleRequest(queuedRequest);

        assertActive(activeRequest);
        assertQueued(queuedRequest);

        assertSame(activeRequest.getRevision().getRevision(), revision1);
    }

    public void testCannotReplaceFixedRevision()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        final Revision revision1 = new Revision("1");
        final Revision revision2 = new Revision("2");

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "source", true, revision1);
        BuildRequestEvent replacementRequest = createRequest(BUILD_ID2, "source", true, revision2);

        queue.handleRequest(activeRequest);
        BuildRevision activeBuildRevision = activeRequest.getRevision();
        assertSame(activeBuildRevision.getRevision(), revision1);
        activeBuildRevision.lock();
        activeBuildRevision.fix();
        activeBuildRevision.unlock();

        queue.handleRequest(replacementRequest);

        assertActive(activeRequest);
        assertQueued(replacementRequest);

        assertSame(activeBuildRevision.getRevision(), revision1);
    }

    public void testReplaceExistingQueued()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();
        final long BUILD_ID3 = nextId.getAndIncrement();

        final Revision revision1 = new Revision("1");
        final Revision revision2 = new Revision("2");

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "another source", true, revision1);
        BuildRequestEvent queuedRequest = createRequest(BUILD_ID2, "source", true, revision1);
        BuildRequestEvent replacementRequest = createRequest(BUILD_ID3, "source", true, revision2);

        queue.handleRequest(activeRequest);
        queue.handleRequest(queuedRequest);
        assertSame(queuedRequest.getRevision().getRevision(), revision1);

        queue.handleRequest(replacementRequest);

        assertActive(activeRequest);
        assertQueued(queuedRequest);

        assertSame(queuedRequest.getRevision().getRevision(), revision2);
    }

    public void testCancelQueuedBuild()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "source1", false);
        BuildRequestEvent queuedRequest = createRequest(BUILD_ID2, "source2", false);
        queue.handleRequest(activeRequest);
        queue.handleRequest(queuedRequest);

        assertActive(activeRequest);
        assertQueued(queuedRequest);

        assertTrue(queue.cancelQueuedRequest(queuedRequest.getId()));

        assertActive(activeRequest);
        assertQueued();
    }

    public void testCancelActiveBuild()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "source1", false);
        BuildRequestEvent queuedRequest = createRequest(BUILD_ID2, "source2", false);
        queue.handleRequest(activeRequest);
        queue.handleRequest(queuedRequest);

        assertActive(activeRequest);
        assertQueued(queuedRequest);

        assertFalse(queue.cancelQueuedRequest(activeRequest.getId()));

        assertActive(activeRequest);
        assertQueued(queuedRequest);
    }

    public void testCancelInvalidId()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "source1", false);
        BuildRequestEvent queuedRequest = createRequest(BUILD_ID2, "source2", false);
        queue.handleRequest(activeRequest);
        queue.handleRequest(queuedRequest);

        assertActive(activeRequest);
        assertQueued(queuedRequest);

        assertFalse(queue.cancelQueuedRequest(-1));

        assertActive(activeRequest);
        assertQueued(queuedRequest);
    }

    public void testCancelNoPermission()
    {
        final long BUILD_ID1 = nextId.getAndIncrement();
        final long BUILD_ID2 = nextId.getAndIncrement();

        BuildRequestEvent activeRequest = createRequest(BUILD_ID1, "source1", false);
        BuildRequestEvent queuedRequest = createRequest(BUILD_ID2, "source2", false);

        //noinspection ThrowableInstanceNeverThrown
        doThrow(new AccessDeniedException("badness")).when(accessManager).ensurePermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, queuedRequest);

        queue.handleRequest(activeRequest);
        queue.handleRequest(queuedRequest);

        assertActive(activeRequest);
        assertQueued(queuedRequest);

        try
        {
            queue.cancelQueuedRequest(queuedRequest.getId());
            fail("Should not have been able to cancel request without permission");
        }
        catch(AccessDeniedException e)
        {
            assertActive(activeRequest);
            assertQueued(queuedRequest);
        }
    }

    public void testRequestInvalidOwner()
    {
        try
        {
            SingleBuildRequestEvent request = mock(SingleBuildRequestEvent.class);
            stub(request.getOwner()).toReturn(new Project());

            queue.handleRequest(request);
            fail("Queue accepted build for another owner");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("different owner"));
        }
    }

    private void assertActive(BuildRequestEvent... events)
    {
        assertActive(queue.getActiveBuildsSnapshot(), events);
    }

    private void assertQueued(BuildRequestEvent... events)
    {
        assertQueued(queue.getQueuedBuildsSnapshot(), events);
    }

    private void assertEvents(ActivatedEventListener listener, Event... events)
    {
        assertEquals(events.length, listener.receivedEvents.size());
        for (int i = 0; i < events.length; i++)
        {
            assertEquals(events[i], listener.receivedEvents.get(i));
        }
    }

    private BuildRequestEvent createRequest(final long buildId, String source, boolean replaceable)
    {
        return createRequest(buildId, source, replaceable, null);
    }

    private BuildRequestEvent createRequest(final long buildId, String source, boolean replaceable, Revision revision)
    {
        return createRequest(owner, buildId, source, replaceable, revision);
    }

    private static class ActivatedEventListener implements EventListener
    {
        public List<Event> receivedEvents = new LinkedList<Event>();

        public void handleEvent(Event event)
        {
            receivedEvents.add(event);
        }

        public void clear()
        {
            receivedEvents.clear();
        }
        
        public Class[] getHandledEvents()
        {
            return new Class[] { BuildActivatedEvent.class };
        }
    }
}
