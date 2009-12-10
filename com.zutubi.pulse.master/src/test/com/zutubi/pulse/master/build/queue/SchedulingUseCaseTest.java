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
    private Project standalone;
    private Project utility;
    private Project libraryA;
    private Project libraryB;
    private Project client;

    private SequenceManager sequenceManager;
    private SchedulingController controller;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        standalone = createProject("standalone");
        utility = createProject("utility");
        libraryA = createProject("libraryA", dependency(utility));
        libraryB = createProject("libraryB", dependency(utility));
        client = createProject("client", dependency(libraryA), dependency(libraryB));

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
        BuildRequestEvent request = createRequest(standalone);

        controller.handleEvent(request);
        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, standalone);

        assertQueued();
        assertActivated(request);
        assertActivatedEvents(request);

        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(buildRequestRegistry, times(1)).requestActivated(request, request.getId());

        controller.handleEvent(createSuccessful(request));

        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.IDLE);
        verify(projectManager, times(2)).makeStateTransition(anyLong(), (Project.Transition)anyObject()); // ensure only the two transitions.
        setProjectState(State.IDLE, standalone);

        assertActivated();
        assertQueued();
    }

    public void testMultipleRequestsForSingleProject()
    {
        BuildRequestEvent requestA = createRequest(standalone);
        BuildRequestEvent requestB = createRequest(standalone);
        BuildRequestEvent requestC = createRequest(standalone);

        controller.handleEvent(requestA);
        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, standalone);
        
        controller.handleEvent(requestB);
        controller.handleEvent(requestC);
        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.BUILDING);

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

        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.IDLE);
        verify(projectManager, times(2)).makeStateTransition(anyLong(), (Project.Transition)anyObject()); // ensure only the two transitions.
        setProjectState(State.IDLE, standalone);

        assertActivated();
        assertQueued();
    }

    public void testRebuildOfProjectTree()
    {
        BuildRequestEvent request = createRebuildRequest(client);

        controller.handleEvent(request);

        assertEquals(3, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(projectManager, times(1)).makeStateTransition(libraryA.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, utility, libraryA, libraryB, client);

        // complete build B..., ensure that C and D are activated, E remains queued.
        controller.handleEvent(createSuccessful(request.getMetaBuildId(), utility));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.IDLE);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), libraryA));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(libraryA.getId(), Transition.IDLE);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), libraryB));

        assertActivated(request);
        assertQueued();
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.IDLE);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), client));
        assertActivated();
        assertQueued();
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.IDLE);
    }

    public void testRebuildOfProjectTreeHasFailure()
    {
        BuildRequestEvent request = createRebuildRequest(client);
        controller.handleEvent(request);

        assertEquals(3, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        setProjectState(State.BUILDING, utility, libraryA, libraryB, client);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), utility));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.IDLE);

        controller.handleEvent(createFailed(request.getMetaBuildId(), libraryA));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(libraryA.getId(), Transition.IDLE);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.IDLE);
        verify(buildRequestRegistry, times(1)).requestCancelled(request);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), libraryB));
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.IDLE);
    }

    public void testRebuildOfProjectRejectedBecauseProjectPaused()
    {
        setProjectState(State.PAUSED, libraryA);
        BuildRequestEvent request = createRebuildRequest(client);
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
