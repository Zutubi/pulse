package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.events.build.BuildActivatedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Arrays;

public class BuildQueueTest extends BaseQueueTestCase
{
    private BuildQueue buildQueue;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildQueue = objectFactory.buildBean(BuildQueue.class);
    }

    public void testRequestsAreQueuedInOrder()
    {
        QueuedRequest r1 = activeRequest("a");
        QueuedRequest r2 = queueRequest("b");
        QueuedRequest r3 = queueRequest("c");
        QueuedRequest r4 = queueRequest("d");

        buildQueue.enqueue(r1, r2, r3, r4);

        List<QueuedRequest> queuedRequests = buildQueue.getQueuedRequests();
        assertEquals(r4, queuedRequests.get(0));
        assertEquals(r3, queuedRequests.get(1));
        assertEquals(r2, queuedRequests.get(2));
    }

    public void testCancelQueuedRequest()
    {
        QueuedRequest r1 = queueRequest("a");

        buildQueue.enqueue(r1);
        assertQueued(r1.getRequest());

        assertTrue(buildQueue.cancel(r1.getRequest().getId()));
        assertQueued();
    }

    public void testCancelActivatedRequest()
    {
        QueuedRequest r1 = activeRequest("a");

        buildQueue.enqueue(r1);
        assertActivated(r1.getRequest());

        assertFalse(buildQueue.cancel(r1.getRequest().getId()));
        assertActivated(r1.getRequest());
    }

    public void testCancelUnknownRequest()
    {
        assertFalse(buildQueue.cancel(123));
    }

    public void testCompleteQueuedRequest()
    {
        QueuedRequest r1 = queueRequest("a");

        buildQueue.enqueue(r1);
        assertQueued(r1.getRequest());

        assertFalse(buildQueue.complete(r1.getRequest().getId()));
        assertQueued(r1.getRequest());
    }

    public void testCompleteActivatedRequest()
    {
        QueuedRequest r1 = activeRequest("a");

        buildQueue.enqueue(r1);
        assertActivated(r1.getRequest());

        assertTrue(buildQueue.complete(r1.getRequest().getId()));
        assertActivated();
    }

    public void testCompleteUnknownRequest()
    {
        assertFalse(buildQueue.complete(123));
    }

    public void testBuildRegistryUpdatedOnQueue()
    {
        QueuedRequest r1 = queueRequest("a");
        buildQueue.enqueue(r1);
        verify(buildRequestRegistry, times(1)).requestQueued(r1.getRequest());
    }

    public void testBuildRegistryUpdatedOnActivation()
    {
        QueuedRequest r1 = activeRequest("a");
        buildQueue.enqueue(r1);
        verify(buildRequestRegistry, times(1)).requestActivated(r1.getRequest(), r1.getRequest().getId());
    }

    public void testBuildRegistryUpdatedOnCancel()
    {
        QueuedRequest r1 = queueRequest("a");
        buildQueue.enqueue(r1);
        buildQueue.cancel(r1.getRequest().getId());
        verify(buildRequestRegistry, times(1)).requestCancelled(r1.getRequest());
    }

    public void testPauseQueueHaltsActivation()
    {
        QueuedRequest r1 = activeRequest("a");
        QueuedRequest r2 = activeRequest("b");
        QueuedRequest r3 = activeRequest("c");

        //noinspection SynchronizeOnNonFinalField
        synchronized (buildQueue)
        {
            buildQueue.pauseActivation();
            buildQueue.enqueue(r1, r2, r3);
            assertQueued(r1.getRequest(), r2.getRequest(), r3.getRequest());
            assertActivated();

            buildQueue.resumeActivation();
            assertQueued();
            assertActivated(r1.getRequest(), r2.getRequest(), r3.getRequest());
        }
    }

    public void testMustHaveQueueSynchronisedToPause()
    {
        try
        {
            buildQueue.pauseActivation();
            fail("expected illegal state exception");
        }
        catch (IllegalStateException e)
        {
            // noop.
        }
    }

    public void testMustHaveQueueSynchronisedToResume()
    {
        try
        {
            buildQueue.resumeActivation();
            fail("expected illegal state exception");
        }
        catch (IllegalStateException e)
        {
            // noop.
        }
    }

    public void testRequestActivationGeneratesEvent()
    {
        QueuedRequest r1 = activeRequest("a");

        assertEquals(0, listener.getReceivedCount());
        buildQueue.enqueue(r1);
        assertEquals(1, listener.getReceivedCount());
        BuildActivatedEvent activatedEvent = listener.getEventsReceived(BuildActivatedEvent.class).get(0);
        assertEquals(r1.getRequest(), activatedEvent.getEvent());
    }

    public void testAssimilationOfNewQueueRequestIntoActivatedRequest()
    {
        Project projectA = createProject("a");

        BuildRequestEvent requestA = createRequest(projectA, "sourceA", true, new Revision("1"));
        BuildRequestEvent requestB = createRequest(projectA, "sourceA", true, new Revision("2"));

        BuildController controller = controllers.get(requestA);
        doReturn(true).when(controller).updateRevisionIfNotFixed(new Revision("2"));

        buildQueue.enqueue(active(requestA));
        buildQueue.enqueue(queue(requestB));

        assertActivated(requestA);
        assertQueued();

        verify(buildRequestRegistry, times(1)).requestAssimilated(requestB, requestA.getId());
        verify(controller, times(1)).updateRevisionIfNotFixed(new Revision("2"));
    }

    public void testAssimilationOfQueuedRequestsDuringActivation()
    {
        Project projectA = createProject("a");

        BuildRequestEvent requestA = createRequest(projectA, "sourceA", true, new Revision("1"));
        BuildRequestEvent requestB = createRequest(projectA, "sourceA", true, new Revision("2"));

        BuildController controller = controllers.get(requestA);
        doReturn(true).when(controller).updateRevisionIfNotFixed((Revision) anyObject());

        buildQueue.enqueue(active(requestA), queue(requestB));

        assertActivated(requestA);
        assertQueued();

        verify(buildRequestRegistry, times(1)).requestAssimilated(requestB, requestA.getId());
    }

    public void testAssimilationRequiresSameSource()
    {
        Project projectA = createProject("a");

        BuildRequestEvent requestA = createRequest(projectA, "sourceA", true, new Revision("1"));
        BuildRequestEvent requestB = createRequest(projectA, "sourceB", true, new Revision("2"));

        BuildController controller = controllers.get(requestA);
        doReturn(true).when(controller).updateRevisionIfNotFixed((Revision) anyObject());

        buildQueue.enqueue(active(requestA), queue(requestB));

        assertActivated(requestA);
        assertQueued(requestB);

        verify(buildRequestRegistry, never()).requestAssimilated((BuildRequestEvent) anyObject(), anyLong());
    }

    public void testRequestsFromDifferentOwnersNotAssimilated()
    {
        BuildRequestEvent requestA = createRequest(createProject("A"), "sourceA", true, new Revision("1"));
        BuildRequestEvent requestB = createRequest(createProject("B"), "sourceA", true, new Revision("2"));

        BuildController controller = controllers.get(requestA);
        doReturn(true).when(controller).updateRevisionIfNotFixed((Revision) anyObject());

        buildQueue.enqueue(active(requestA), queue(requestB));

        assertActivated(requestA);
        assertQueued(requestB);

        verify(buildRequestRegistry, never()).requestAssimilated(requestB, requestA.getId());
    }

    public void testQueuedRequestsWithPendingDependenciesNotAssimilated()
    {
        Project projectA = createProject("a");
        Project projectB = createProject("b");

        BuildRequestEvent requestA = createRequest(projectA, "sourceA", true, new Revision("1"));
        BuildRequestEvent requestB = createRequest(projectA, "sourceA", true, new Revision("2"));
        BuildRequestEvent requestX = createRequest(projectB, "sourceA", true, new Revision("3"));

        BuildController controller = controllers.get(requestA);
        doReturn(true).when(controller).updateRevisionIfNotFixed((Revision) anyObject());

        buildQueue.enqueue(active(requestX), active(requestA), queue(requestB, new DependencyCompleteQueuePredicate(buildQueue, projectB)));

        assertActivated(requestX, requestA);
        assertQueued(requestB);

        verify(buildRequestRegistry, never()).requestAssimilated((BuildRequestEvent) anyObject(), anyLong());
    }

    public void testSimultaniousSingleProjectRequestProcessedSerially()
    {
        QueuedRequest r1 = new QueuedRequest(createRequest("a"), new ActivateIfIdlePredicate(buildQueue));
        QueuedRequest r2 = new QueuedRequest(createRequest("a"), new ActivateIfIdlePredicate(buildQueue));

        buildQueue.enqueue(r1, r2);

        assertActivated(r1.getRequest());
        assertQueued(r2.getRequest());
    }

    private void assertQueued(BuildRequestEvent... requests)
    {
        assertItemsSame(Arrays.asList(requests), buildQueue.getSnapshot().getQueuedBuildRequests());
    }

    private void assertActivated(BuildRequestEvent... requests)
    {
        assertItemsSame(Arrays.asList(requests), buildQueue.getSnapshot().getActivatedBuildRequests());
    }

    private static class ActivateIfIdlePredicate implements QueuedRequestPredicate
    {
        private BuildQueue queue;

        private ActivateIfIdlePredicate(BuildQueue queue)
        {
            this.queue = queue;
        }

        public boolean satisfied(QueuedRequest queuedRequest)
        {
            return queue.getActivatedRequestCount() == 0;
        }
    }
}
