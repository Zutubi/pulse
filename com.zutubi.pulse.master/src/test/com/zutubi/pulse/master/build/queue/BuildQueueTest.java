package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.events.build.BuildActivatedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

    public void testAssimilationIntoActivatedRequest()
    {
        Project projectA = createProject("a");

        BuildRequestEvent requestA = createRequest(projectA, "sourceA", true, null);
        BuildRequestEvent requestB = createRequest(projectA, "sourceA", true, null);

        buildQueue.enqueue(active(requestA));
        buildQueue.enqueue(queue(requestB));

        assertActivated(requestA);
        assertQueued();

        verify(buildRequestRegistry, times(1)).requestAssimilated(requestB, requestA.getId());
    }

    public void testAssimilationIntoQueuedRequest()
    {
        queuedAssimilationTest(createProject("utility"));
    }

    public void testAssimilationOfTwoRelatedRequests()
    {
        queuedAssimilationTest(createProject("utility"), createProject("client"));
    }

    public void testAssimilationOfMultipleRelatedRequests()
    {
        queuedAssimilationTest(createProject("utility"), createProject("lib"), createProject("client"));
    }

    private void queuedAssimilationTest(Project... projects)
    {
        List<BuildRequestEvent> requests = new LinkedList<BuildRequestEvent>();
        for (Project p : projects)
        {
            requests.add(createRequest(p, "sourceA", true, null));
        }

        buildQueue.enqueue(CollectionUtils.map(requests, new Mapping<BuildRequestEvent, QueuedRequest>()
        {
            public QueuedRequest map(BuildRequestEvent request)
            {
                return queue(request);
            }
        }));

        assertActivated();
        assertQueued(requests.toArray(new BuildRequestEvent[requests.size()]));

        List<BuildRequestEvent> requestsToBeAssimilated = new LinkedList<BuildRequestEvent>();
        for (Project p : projects)
        {
            requestsToBeAssimilated.add(createRequest(p, "sourceA", true, null));
        }

        buildQueue.enqueue(CollectionUtils.map(requestsToBeAssimilated, new Mapping<BuildRequestEvent, QueuedRequest>()
        {
            public QueuedRequest map(BuildRequestEvent request)
            {
                return queue(request);
            }
        }));

        assertActivated();
        assertQueued(requests.toArray(new BuildRequestEvent[requests.size()]));

        for (int i = 0; i < projects.length; i++)
        {
            verify(buildRequestRegistry, times(1)).requestAssimilated(requestsToBeAssimilated.get(i), requests.get(i).getId());
        }
    }

    public void testNoAssimilationIfSomeRequestsCanNotAssimilate()
    {
        Project utilityProject = createProject("utility");
        Project clientProject = createProject("client");

        BuildRequestEvent utilityRequestA = createRequest(utilityProject, "sourceA", true, null);
        BuildRequestEvent clientRequestA = createRequest(clientProject, "sourceA", true, null);

        buildQueue.enqueue(queue(utilityRequestA), queue(clientRequestA));

        assertActivated();
        assertQueued(utilityRequestA, clientRequestA);

        BuildRequestEvent utilityRequestB = createRequest(utilityProject, "sourceA", true, null);
        BuildRequestEvent clientRequestB = createRequest(clientProject, "sourceB", true, null);

        buildQueue.enqueue(queue(utilityRequestB), queue(clientRequestB));

        assertActivated();
        assertQueued(utilityRequestA, clientRequestA, utilityRequestB, clientRequestB);

        verify(buildRequestRegistry, never()).requestAssimilated((BuildRequestEvent) anyObject(), anyLong());
    }

    public void testNoAssimilationIfSourcesAreDifferent()
    {
        Project projectA = createProject("a");

        BuildRequestEvent requestA = createRequest(projectA, "sourceA", true, null);
        BuildRequestEvent requestB = createRequest(projectA, "sourceB", true, null);

        buildQueue.enqueue(active(requestA), queue(requestB));

        assertActivated(requestA);
        assertQueued(requestB);

        verify(buildRequestRegistry, never()).requestAssimilated((BuildRequestEvent) anyObject(), anyLong());
    }

    public void testNoAssimilationIfOwnersAreDifferent()
    {
        BuildRequestEvent requestA = createRequest(createProject("A"), "sourceA", true, null);
        BuildRequestEvent requestB = createRequest(createProject("B"), "sourceA", true, null);

        buildQueue.enqueue(active(requestA), queue(requestB));

        assertActivated(requestA);
        assertQueued(requestB);

        verify(buildRequestRegistry, never()).requestAssimilated(requestB, requestA.getId());
    }

    public void testNoAssimilationIfReplaceableOnTargetRequestIsFalse()
    {
        Project project = createProject("A");
        BuildRequestEvent requestA = createRequest(project, "sourceA", false, null);
        BuildRequestEvent requestB = createRequest(project, "sourceA", true, null);

        buildQueue.enqueue(active(requestA));
        buildQueue.enqueue(queue(requestB));

        assertActivated(requestA);
        assertQueued(requestB);

        verify(buildRequestRegistry, never()).requestAssimilated(requestB, requestA.getId());
    }

    public void testNoAssimilationIfReplaceableOnSourceRequestIsFalse()
    {
        Project project = createProject("A");
        BuildRequestEvent requestA = createRequest(project, "sourceA", true, null);
        BuildRequestEvent requestB = createRequest(project, "sourceA", false, null);

        buildQueue.enqueue(active(requestA));
        buildQueue.enqueue(queue(requestB));

        assertActivated(requestA);
        assertQueued(requestB);

        verify(buildRequestRegistry, never()).requestAssimilated(requestB, requestA.getId());
    }

    public void testNoAssimilationIfBuildCommenced()
    {
        Project project = createProject("A");
        BuildRequestEvent requestA = createRequest(project, "sourceA", true, new Revision("1"));

        BuildRequestEvent requestB = createRequest(project, "sourceA", true, null);

        buildQueue.enqueue(active(requestA));
        buildQueue.commencing(createBuildResult(requestA).getId());
        buildQueue.enqueue(queue(requestB));

        assertActivated(requestA);
        assertQueued(requestB);

        verify(buildRequestRegistry, never()).requestAssimilated(requestB, requestA.getId());
    }

    public void testQueuedRequestsWithPendingDependenciesNotAssimilated()
    {
        Project projectA = createProject("a");
        Project projectB = createProject("b");

        BuildRequestEvent requestA = createRequest(projectA, "sourceA", true, null);
        BuildRequestEvent requestB = createRequest(projectA, "sourceA", true, null);
        BuildRequestEvent requestX = createRequest(projectB, "sourceA", true, null);

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

    public void testExceptionInControllerStart()
    {
        QueuedRequest request = exceptionRequest("a");
        buildQueue.enqueue(request);

        assertActivated();
        assertQueued();
        verify(buildRequestRegistry, times(1)).requestRejected(eq(request.getRequest()), anyString());
        verify(buildRequestRegistry, never()).requestActivated((BuildRequestEvent) anyObject(), anyLong());
    }

    public void testBuildQueueWorksAfterControllerStartException()
    {
        String projectName = "a";
        
        QueuedRequest request = exceptionRequest(projectName);
        buildQueue.enqueue(request);

        assertActivated();
        assertQueued();

        QueuedRequest requestB = activeRequest(projectName);
        QueuedRequest requestC = queueRequest(projectName);

        buildQueue.enqueue(requestB);
        buildQueue.enqueue(requestC);
        assertQueued(requestC.getRequest());
        assertActivated(requestB.getRequest());

        assertTrue(buildQueue.complete(requestB.getRequest().getId()));

        assertActivated();
        assertQueued(requestC.getRequest());

        assertTrue(buildQueue.cancel(requestC.getRequest().getId()));

        assertActivated();
        assertQueued();

        QueuedRequest requestD = activeRequest("other");
        buildQueue.enqueue(requestD);

        assertActivated(requestD.getRequest());
    }

    public QueuedRequest exceptionRequest(String projectName)
    {
        QueuedRequest request = activeRequest(projectName);
        BuildController controller = controllers.get(request.getRequest());
        doReturn(0).when(controller).start();
        return request;
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

        public boolean apply(QueuedRequest queuedRequest)
        {
            return queue.getActivatedRequestCount() == 0;
        }
    }
}
