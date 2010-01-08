package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildActivatedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import static com.zutubi.pulse.master.model.Project.State;
import static com.zutubi.pulse.master.model.Project.Transition;
import com.zutubi.pulse.master.model.Sequence;
import com.zutubi.pulse.master.model.SequenceManager;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.eq;

import java.util.List;
import java.util.Arrays;

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

        assertQueuedCount(3);
        assertActivatedCount(1);
        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(projectManager, times(1)).makeStateTransition(libraryA.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, utility, libraryA, libraryB, client);

        // complete build B..., ensure that C and D are activated, E remains queued.
        controller.handleEvent(createSuccessful(request.getMetaBuildId(), utility));

        assertQueuedCount(1);
        assertActivatedCount(2);
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.IDLE);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), libraryA));

        assertQueuedCount(1);
        assertActivatedCount(1);
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

        assertQueuedCount(3);
        assertActivatedCount(1);
        setProjectState(State.BUILDING, utility, libraryA, libraryB, client);

        controller.handleEvent(createSuccessful(request.getMetaBuildId(), utility));

        assertQueuedCount(1);
        assertActivatedCount(2);
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.IDLE);

        controller.handleEvent(createFailed(request.getMetaBuildId(), libraryA));

        assertQueuedCount(0);
        assertActivatedCount(1);
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

    public void testMultipleTriggersAreAssimilated()
    {
        // We have a cron trigger that kicks off regular builds.  These builds are slower than the
        // schedule, so the excess cron triggers should be assimilated.

        BuildRequestEvent triggerA = createRequest(client, "cronTrigger", true, new Revision("1"));
        controller.handleEvent(triggerA);

        assertActivated(triggerA);
        setProjectState(State.BUILDING, client);

        // The triggerd requests queue up.
        BuildRequestEvent triggerB = createRequest(client, "cronTrigger", true, new Revision("1"));
        controller.handleEvent(triggerB);
        assertQueued(triggerB);
        BuildRequestEvent triggerC = createRequest(client, "cronTrigger", true, new Revision("2"));
        controller.handleEvent(triggerC);
        assertQueued(triggerB, triggerC);

        controller.handleEvent(createSuccessful(triggerA.getMetaBuildId(), client));

        assertQueued();
        assertActivated(triggerB);

        verify(buildRequestRegistry, times(1)).requestActivated(triggerA, triggerA.getId());
        verify(buildRequestRegistry, times(1)).requestActivated(triggerB, triggerB.getId());
        verify(buildRequestRegistry, times(1)).requestAssimilated(triggerC, triggerB.getId());
    }

    public void testQueueJumpingEnabled()
    {
        BuildRequestEvent requestA = createRequest(libraryB);
        controller.handleEvent(requestA);

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.BUILDING);
        verify(buildRequestRegistry, times(1)).requestQueued(requestA);
        setProjectState(State.BUILDING, libraryB, client);

        BuildRequestEvent requestB = createRequest(client);
        controller.handleEvent(requestB);
        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(buildRequestRegistry, times(1)).requestActivated(requestB, requestB.getId());
    }

    public void testQueueJumpingCanBeDisabled()
    {
        BuildRequestEvent requestA = createRequest(libraryB);
        controller.handleEvent(requestA);

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.BUILDING);
        verify(buildRequestRegistry, times(1)).requestQueued(requestA);
        setProjectState(State.BUILDING, libraryB, client);

        BuildRequestEvent requestB = createRequest(client);
        requestB.getOptions().setJumpQueueAllowed(false);
        controller.handleEvent(requestB);
        assertEquals(2, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(buildRequestRegistry, times(1)).requestQueued(requestB);
    }

    /**
     * This test case is prompted by a user request.  The situation is that on release of
     * software, they touch all of there components with a trivial update of a version string.
     * Pulse picks this up as separate scm changes and proceeds to launch a tonne of builds.
     *
     * We need to ensure that so long as the requests are correctly configured, they are assimilated
     * with minimal fuss.
     */
    public void testOverlappingScmCommitsAreAssimilated()
    {
        BuildRequestEvent utilScmChange = createRequest(utility, "scm change", true, false);
        BuildRequestEvent libAScmChange = createRequest(libraryA, "scm change", true, false);
        BuildRequestEvent libBScmChange = createRequest(libraryB, "scm change", true, false);
        BuildRequestEvent clientScmChange = createRequest(client, "scm change", true, false);

        controller.handleEvent(utilScmChange);
        controller.handleEvent(libAScmChange);
        controller.handleEvent(libBScmChange);
        controller.handleEvent(clientScmChange);

        assertEquals(8, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        verify(buildRequestRegistry, times(1)).requestActivated(utilScmChange, utilScmChange.getId());
        verify(buildRequestRegistry, times(1)).requestQueued(libAScmChange);
        verify(buildRequestRegistry, times(1)).requestQueued(libBScmChange);
        verify(buildRequestRegistry, times(1)).requestQueued(clientScmChange);

        controller.handleEvent(createSuccessful(utilScmChange.getMetaBuildId(), utility));

        assertEquals(4, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());

        verify(buildRequestRegistry, times(1)).requestAssimilated(eq(libAScmChange), anyLong());
        verify(buildRequestRegistry, times(1)).requestAssimilated(eq(libBScmChange), anyLong());

        controller.handleEvent(createSuccessful(utilScmChange.getMetaBuildId(), libraryA));

        assertEquals(4, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleEvent(createSuccessful(utilScmChange.getMetaBuildId(), libraryB));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleEvent(createSuccessful(utilScmChange.getMetaBuildId(), client));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(0, controller.getSnapshot().getActivatedBuildRequests().size());
    }

    /**
     * Two scm changes to the same component, ensure that none of the second changes
     * requests are assimilated into the first changes build.
     */
    public void testMultipleOverlappingCommitsRemainDistinct()
    {
        BuildRequestEvent changeA = createRequest(utility, "scm change", true, false);
        BuildRequestEvent changeB = createRequest(utility, "scm change", true, false);

        controller.handleEvent(changeA);
        controller.handleEvent(changeB);

        assertEquals(7, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        verify(buildRequestRegistry, times(1)).requestActivated(changeA, changeA.getId());
        verify(buildRequestRegistry, times(1)).requestQueued(changeB);

        controller.handleEvent(createSuccessful(changeA.getMetaBuildId(), utility));

        assertEquals(4, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(3, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleEvent(createSuccessful(changeA.getMetaBuildId(), libraryA));

        assertEquals(4, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleEvent(createSuccessful(changeA.getMetaBuildId(), libraryB));

        assertEquals(3, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleEvent(createSuccessful(changeA.getMetaBuildId(), client));

        assertEquals(3, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        verify(buildRequestRegistry, never()).requestAssimilated((BuildRequestEvent) anyObject(), anyLong());
    }

    private void assertActivatedCount(int expectedCount)
    {
        assertEquals(expectedCount, controller.getSnapshot().getActivatedBuildRequests().size());
    }

    private void assertQueuedCount(int expectedCount)
    {
        assertEquals(expectedCount, controller.getSnapshot().getQueuedRequests().size());
    }

    private void assertActivated(BuildRequestEvent... requests)
    {
        List<BuildRequestEvent> activatedRequests = controller.getSnapshot().getActivatedBuildRequests();
        assertItemsSame(Arrays.asList(requests), activatedRequests);
    }

    private void assertQueued(BuildRequestEvent... requests)
    {
        List<BuildRequestEvent> queuedRequests = controller.getSnapshot().getQueuedBuildRequests();
        assertItemsSame(Arrays.asList(requests), queuedRequests);
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
        assertItemsSame(Arrays.asList(requests), activatedEvents);
    }
}
