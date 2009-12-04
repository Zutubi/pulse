package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildActivatedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import static com.zutubi.pulse.master.model.Project.State;
import static com.zutubi.pulse.master.model.Project.Transition;
import com.zutubi.pulse.master.model.Sequence;
import com.zutubi.pulse.master.model.SequenceManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import static org.mockito.Mockito.*;

import java.util.List;

/**
 * Check that a set of use cases behave as expected.
 *
 * (unit level integration test for this packages components)
 */
public class SchedulingUseCaseTest extends BaseQueueTestCase
{
    /* Project configuration is as follows:  The details of the dependency relationships
       are modified as necessary by the individual tests.

       A

          - C -
        /      \
       B        E
        \      /
         - D -

     */

    private Project A;
    private Project B;
    private Project C;
    private Project D;
    private Project E;

    private SequenceManager sequenceManager;
    private SchedulingController controller;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        A = createProject("A");
        B = createProject("B");
        C = createProject("C", dependency(B));
        D = createProject("D", dependency(B));
        E = createProject("E", dependency(C), dependency(D));

        sequenceManager = mock(SequenceManager.class);
        stub(sequenceManager.getSequence(anyString())).toReturn(new Sequence()
        {
            public long getNext()
            {
                return nextId.getAndIncrement();
            }
        });
        objectFactory.initProperties(this);

        controller = objectFactory.buildBean(SchedulingController.class);
        controller.setBuildQueue(objectFactory.buildBean(BuildQueue.class));
    }

    public void testBuildRequestForIsolatedProject()
    {
        BuildRequestEvent request = createRequest("A");

        controller.handleEvent(request);
        verify(projectManager, times(1)).makeStateTransition(A.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, A);

        assertQueued();
        assertActivated(request);
        assertActivatedEvents(request);

        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(buildRequestRegistry, times(1)).requestActivated(request, request.getId());

        controller.handleEvent(createSuccessful(request));

        verify(projectManager, times(1)).makeStateTransition(A.getId(), Transition.IDLE);
        verify(projectManager, times(2)).makeStateTransition(anyLong(), (Project.Transition)anyObject()); // ensure only the two transitions.
        setProjectState(State.IDLE, A);

        assertActivated();
        assertQueued();
    }

    public void testMultipleRequestsForSingleProject()
    {
        BuildRequestEvent requestA = createRequest("A");
        BuildRequestEvent requestB = createRequest("A");
        BuildRequestEvent requestC = createRequest("A");

        controller.handleEvent(requestA);
        verify(projectManager, times(1)).makeStateTransition(A.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, A);
        
        controller.handleEvent(requestB);
        controller.handleEvent(requestC);
        verify(projectManager, times(1)).makeStateTransition(A.getId(), Transition.BUILDING);

        // activated event was generated.
        assertQueued(requestB, requestC);
        assertActivated(requestA);
        assertActivatedEvents(requestA);

        verify(buildRequestRegistry, times(1)).requestQueued(requestA);
        verify(buildRequestRegistry, times(1)).requestActivated(requestA, requestA.getId());
        verify(buildRequestRegistry, times(1)).requestQueued(requestB);
        verify(buildRequestRegistry, times(1)).requestQueued(requestC);

        controller.handleEvent(createSuccessful(requestA));

        verify(projectManager, times(1)).makeStateTransition(anyLong(), (Project.Transition)anyObject());

        assertQueued(requestC);
        assertActivated(requestB);
        assertActivatedEvents(requestA, requestB);
        verify(buildRequestRegistry, times(1)).requestActivated(requestB, requestB.getId());

        controller.handleEvent(createSuccessful(requestB));

        verify(projectManager, times(1)).makeStateTransition(anyLong(), (Project.Transition)anyObject());
        
        assertQueued();
        assertActivated(requestC);
        assertActivatedEvents(requestA, requestB, requestC);
        verify(buildRequestRegistry, times(1)).requestActivated(requestC, requestC.getId());

        controller.handleEvent(createSuccessful(requestC));

        verify(projectManager, times(1)).makeStateTransition(A.getId(), Transition.IDLE);
        verify(projectManager, times(2)).makeStateTransition(anyLong(), (Project.Transition)anyObject()); // ensure only the two transitions.
        setProjectState(State.IDLE, A);

        assertActivated();
        assertQueued();
    }

    public void testRebuildOfProjectTree()
    {
        BuildRequestEvent request = createRebuildRequest(E);

        controller.handleEvent(request);

        assertEquals(3, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(projectManager, times(1)).makeStateTransition(C.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(B.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(D.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(E.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, B, C, D, E);

        // complete build B..., ensure that C and D are activated, E remains queued.
        controller.handleEvent(createSuccessful(request.getMetaBuildId(), B));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(B.getId(), Transition.IDLE);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), C));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(C.getId(), Transition.IDLE);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), D));

        assertActivated(request);
        assertQueued();
        verify(projectManager, times(1)).makeStateTransition(D.getId(), Transition.IDLE);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), E));
        assertActivated();
        assertQueued();
        verify(projectManager, times(1)).makeStateTransition(E.getId(), Transition.IDLE);
    }

    public void testRebuildOfProjectTreeHasFailure()
    {
        BuildRequestEvent request = createRebuildRequest(E);
        controller.handleEvent(request);

        assertEquals(3, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        setProjectState(State.BUILDING, B, C, D, E);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), B));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(B.getId(), Transition.IDLE);

        controller.handleEvent(createFailed(request.getMetaBuildId(), C));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(C.getId(), Transition.IDLE);
        verify(projectManager, times(1)).makeStateTransition(E.getId(), Transition.IDLE);
        verify(buildRequestRegistry, times(1)).requestCancelled(request);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), D));
        verify(projectManager, times(1)).makeStateTransition(D.getId(), Transition.IDLE);
    }

/*
    public void testBuildJumpsQueueBecausePreviousBuildIsWaitingOnDependency()
    {
        // It doesnt jump the queue because the AmIAtTheHeadOfTheQueue fails.
        // - maybe it should be, am i at the head of the queue of requests that are not waiting on a dependency...

        BuildRequestEvent requestA = createRebuildRequest(C);
        controller.handleEvent(requestA);

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(B.getId(), Transition.BUILDING);
        verify(buildRequestRegistry, times(1)).requestQueued(requestA);
        setProjectState(State.BUILDING, B);

        BuildRequestEvent requestB = createRequest(C);
        controller.handleEvent(requestB);

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(C.getId(), Transition.BUILDING);
        verify(buildRequestRegistry, times(1)).requestQueued(requestB);
        verify(buildRequestRegistry, times(1)).requestActivated(requestB, requestB.getId());
        setProjectState(State.BUILDING, C);

        controller.handleEvent(createSuccessful(requestA.getMetaBuildId(), B));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(B.getId(), Transition.IDLE);

        controller.handleEvent(createSuccessful(requestB.getMetaBuildId(), C));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(buildRequestRegistry, times(1)).requestActivated(requestA, requestA.getId());

        controller.handleEvent(createSuccessful(requestA.getMetaBuildId(), C));
        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(0, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(C.getId(), Transition.IDLE);
    }
*/

    public void testRebuildOfProjectRejectedBecauseProjectPaused()
    {
        setProjectState(State.PAUSED, C);
        BuildRequestEvent request = createRebuildRequest(E);
        controller.handleEvent(request);
        verify(buildRequestRegistry, times(1)).requestRejected(eq(request), anyString());
    }

    private void assertActivated(BuildRequestEvent... requests)
    {
        List<BuildRequestEvent> activatedRequests = controller.getSnapshot().getActivatedBuildRequests();
        assertItemsEqual(activatedRequests, requests);
    }

    private void assertQueued(BuildRequestEvent... requests)
    {
        List<BuildRequestEvent> queuedRequests = controller.getSnapshot().getQueuedBuildRequests();
        assertItemsEqual(queuedRequests, requests);
    }

    private void assertActivatedEvents(BuildRequestEvent... requests)
    {
        // activated event was generated.
        List<BuildRequestEvent> activatedEvents = CollectionUtils.map(listener.getEventsReceived(BuildActivatedEvent.class), new Mapping<BuildActivatedEvent, BuildRequestEvent>()
        {
            public BuildRequestEvent map(BuildActivatedEvent buildActivatedEvent)
            {
                return buildActivatedEvent.getEvent();
            }
        });
        assertItemsEqual(activatedEvents, requests);
    }
}
