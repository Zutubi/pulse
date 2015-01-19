package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Function;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.events.build.BuildActivatedEvent;
import com.zutubi.pulse.master.events.build.BuildCommencingEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.BuildRevision;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.Sequence;
import com.zutubi.pulse.master.model.SequenceManager;
import com.zutubi.tove.security.AccessManager;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.zutubi.pulse.master.model.Project.State;
import static com.zutubi.pulse.master.model.Project.Transition;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
        controller.setAccessManager(mock(AccessManager.class));
    }

    public void testBuildRequestForIsolatedProject()
    {
        BuildRequestEvent request = createRequest(standalone);

        controller.handleBuildRequest(request);
        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, standalone);

        assertQueued();
        assertActivated(request);
        assertActivatedEvents(request);

        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(buildRequestRegistry, times(1)).requestActivated(request, request.getId());

        controller.handleBuildCompleted(createSuccessful(request));

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

        controller.handleBuildRequest(requestA);
        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, standalone);
        
        controller.handleBuildRequest(requestB);
        controller.handleBuildRequest(requestC);
        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.BUILDING);

        // activated event was generated.
        assertQueued(requestB, requestC);
        assertActivated(requestA);
        assertActivatedEvents(requestA);

        verify(buildRequestRegistry, times(1)).requestQueued(requestA);
        verify(buildRequestRegistry, times(1)).requestActivated(requestA, requestA.getId());
        verify(buildRequestRegistry, times(1)).requestQueued(requestB);
        verify(buildRequestRegistry, times(1)).requestQueued(requestC);

        controller.handleBuildCompleted(createSuccessful(requestA));

        verify(projectManager, times(1)).makeStateTransition(anyLong(), (Project.Transition)anyObject());

        assertQueued(requestC);
        assertActivated(requestB);
        assertActivatedEvents(requestA, requestB);
        verify(buildRequestRegistry, times(1)).requestActivated(requestB, requestB.getId());

        controller.handleBuildCompleted(createSuccessful(requestB));

        verify(projectManager, times(1)).makeStateTransition(anyLong(), (Project.Transition)anyObject());
        
        assertQueued();
        assertActivated(requestC);
        assertActivatedEvents(requestA, requestB, requestC);
        verify(buildRequestRegistry, times(1)).requestActivated(requestC, requestC.getId());

        controller.handleBuildCompleted(createSuccessful(requestC));

        verify(projectManager, times(1)).makeStateTransition(standalone.getId(), Transition.IDLE);
        verify(projectManager, times(2)).makeStateTransition(anyLong(), (Project.Transition)anyObject()); // ensure only the two transitions.
        setProjectState(State.IDLE, standalone);

        assertActivated();
        assertQueued();
    }

    public void testRebuildOfProjectTree()
    {
        BuildRequestEvent request = createRebuildRequest(client);

        controller.handleBuildRequest(request);

        assertQueuedCount(3);
        assertActivatedCount(1);
        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(projectManager, times(1)).makeStateTransition(libraryA.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, utility, libraryA, libraryB, client);

        // complete build B..., ensure that C and D are activated, E remains queued.
        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), utility));

        assertQueuedCount(1);
        assertActivatedCount(2);
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.IDLE);

        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), libraryA));

        assertQueuedCount(1);
        assertActivatedCount(1);
        verify(projectManager, times(1)).makeStateTransition(libraryA.getId(), Transition.IDLE);

        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), libraryB));

        assertActivated(request);
        assertQueued();
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.IDLE);

        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), client));
        assertActivated();
        assertQueued();
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.IDLE);
    }

    public void testRebuildOfProjectTreeAtFixedVersion()
    {
        Revision revision = new Revision(1111);
        BuildRevision buildRevision = new BuildRevision(revision, true);
        BuildRequestEvent request = createRebuildRequest(client);
        request.setRevision(buildRevision);

        controller.handleBuildRequest(request);

        assertQueuedCount(3);
        assertActivatedCount(1);

        List<QueuedRequest> queuedRequests = controller.getSnapshot().getQueuedRequests();
        for (QueuedRequest queuedRequest : queuedRequests)
        {
            Revision queuedRevision = queuedRequest.getRequest().getRevision().getRevision();
            assertEquals(revision, queuedRevision);
        }
    }

    public void testRebuildOfProjectTreeHasFailure()
    {
        BuildRequestEvent request = createRebuildRequest(client);
        controller.handleBuildRequest(request);

        assertQueuedCount(3);
        assertActivatedCount(1);
        setProjectState(State.BUILDING, utility, libraryA, libraryB, client);

        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), utility));

        assertQueuedCount(1);
        assertActivatedCount(2);
        verify(projectManager, times(1)).makeStateTransition(utility.getId(), Transition.IDLE);

        controller.handleBuildCompleted(createFailed(request.getMetaBuildId(), libraryA));

        assertQueuedCount(0);
        assertActivatedCount(1);
        verify(projectManager, times(1)).makeStateTransition(libraryA.getId(), Transition.IDLE);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.IDLE);
        verify(buildRequestRegistry, times(1)).requestCancelled(request);

        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), libraryB));
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.IDLE);
    }

    public void testRebuildOfProjectRejectedBecauseProjectPaused()
    {
        setProjectState(State.PAUSED, libraryA);
        BuildRequestEvent request = createRebuildRequest(client);
        controller.handleBuildRequest(request);
        verify(buildRequestRegistry, times(1)).requestRejected(eq(request), anyString());
    }

    public void testMultipleTriggersAreAssimilated()
    {
        // We have a cron trigger that kicks off regular builds.  These builds are slower than the
        // schedule, so the excess cron triggers should be assimilated.

        BuildRequestEvent triggerA = createRequest(client, "cronTrigger", true, null);
        controller.handleBuildRequest(triggerA);

        assertActivated(triggerA);
        setProjectState(State.BUILDING, client);

        controller.handleBuildCommencing(new BuildCommencingEvent(this, createBuildResult(triggerA), new PulseExecutionContext()));

        // The triggerd requests queue up.
        BuildRequestEvent triggerB = createRequest(client, "cronTrigger", true, null);
        controller.handleBuildRequest(triggerB);
        assertQueued(triggerB);
        BuildRequestEvent triggerC = createRequest(client, "cronTrigger", true, null);
        controller.handleBuildRequest(triggerC);
        assertQueued(triggerB);

        controller.handleBuildCompleted(createSuccessful(triggerA.getMetaBuildId(), client));

        assertQueued();
        assertActivated(triggerB);

        verify(buildRequestRegistry, times(1)).requestActivated(triggerA, triggerA.getId());
        verify(buildRequestRegistry, times(1)).requestActivated(triggerB, triggerB.getId());
        verify(buildRequestRegistry, times(1)).requestAssimilated(triggerC, triggerB.getId());
    }

    public void testQueueJumpingEnabled()
    {
        BuildRequestEvent requestA = createRequest(libraryB);
        controller.handleBuildRequest(requestA);

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.BUILDING);
        verify(buildRequestRegistry, times(1)).requestQueued(requestA);
        setProjectState(State.BUILDING, libraryB, client);

        BuildRequestEvent requestB = createRequest(client);
        controller.handleBuildRequest(requestB);
        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(buildRequestRegistry, times(1)).requestActivated(requestB, requestB.getId());
    }

    public void testQueueJumpingCanBeDisabled()
    {
        BuildRequestEvent requestA = createRequest(libraryB);
        controller.handleBuildRequest(requestA);

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());
        verify(projectManager, times(1)).makeStateTransition(libraryB.getId(), Transition.BUILDING);
        verify(projectManager, times(1)).makeStateTransition(client.getId(), Transition.BUILDING);
        verify(buildRequestRegistry, times(1)).requestQueued(requestA);
        setProjectState(State.BUILDING, libraryB, client);

        BuildRequestEvent requestB = createRequest(client);
        requestB.getOptions().setJumpQueueAllowed(false);
        controller.handleBuildRequest(requestB);
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
     *
     * Note that each individual scm build request will generate a series of related build requests.
     */
    public void testOverlappingScmCommitsAreAssimilated()
    {
        BuildRequestEvent utilScmChange = createRequest(utility, "scm change", true, false);
        BuildRequestEvent libAScmChange = createRequest(libraryA, "scm change", true, false);
        BuildRequestEvent libBScmChange = createRequest(libraryB, "scm change", true, false);
        BuildRequestEvent clientScmChange = createRequest(client, "scm change", true, false);

        controller.handleBuildRequest(utilScmChange);
        controller.handleBuildRequest(libAScmChange);
        controller.handleBuildRequest(libBScmChange);
        controller.handleBuildRequest(clientScmChange);

        assertEquals(3, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        verify(buildRequestRegistry, times(1)).requestActivated(utilScmChange, utilScmChange.getId());

        controller.handleBuildCompleted(createSuccessful(utilScmChange.getMetaBuildId(), utility));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());

        verify(buildRequestRegistry, times(1)).requestAssimilated(eq(libAScmChange), anyLong());
        verify(buildRequestRegistry, times(1)).requestAssimilated(eq(libBScmChange), anyLong());

        controller.handleBuildCompleted(createSuccessful(utilScmChange.getMetaBuildId(), libraryA));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleBuildCompleted(createSuccessful(utilScmChange.getMetaBuildId(), libraryB));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleBuildCompleted(createSuccessful(utilScmChange.getMetaBuildId(), client));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(0, controller.getSnapshot().getActivatedBuildRequests().size());
    }

    /**
     * Two scm changes to the same component, ensure that the second request assimilates
     * into the first if the first hasn't started (revision fixed).
     */
    public void testMultipleOverlappingCommitsMergeIfTheyCanAssimilate()
    {
        BuildRequestEvent changeA = createRequest(utility, "scm change", true, false);
        BuildRequestEvent changeB = createRequest(utility, "scm change", true, false);

        controller.handleBuildRequest(changeA);
        controller.handleBuildRequest(changeB);

        assertEquals(3, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        verify(buildRequestRegistry, times(1)).requestActivated(changeA, changeA.getId());

        controller.handleBuildCompleted(createSuccessful(changeA.getMetaBuildId(), utility));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(2, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleBuildCompleted(createSuccessful(changeA.getMetaBuildId(), libraryA));

        assertEquals(1, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleBuildCompleted(createSuccessful(changeA.getMetaBuildId(), libraryB));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(1, controller.getSnapshot().getActivatedBuildRequests().size());

        controller.handleBuildCompleted(createSuccessful(changeA.getMetaBuildId(), client));

        assertEquals(0, controller.getSnapshot().getQueuedBuildRequests().size());
        assertEquals(0, controller.getSnapshot().getActivatedBuildRequests().size());

        verify(buildRequestRegistry, times(4)).requestAssimilated((BuildRequestEvent) anyObject(), anyLong());
    }

    /**
     * Two scm changes to the same component, ensure that the second request does not assimilate
     * into the first because the first one has commenced.  Assimilation only occurs if all of a
     * builds associated requests can be assimilated.
     */
    public void testMultipleOverlappingCommitsRemainSeparateIfTheyCanNotAssimilateCompletely()
    {
        BuildRequestEvent changeA = createRequest(utility, "scm change", true, false);
        BuildRequestEvent changeB = createRequest(utility, "scm change", true, false);

        controller.handleBuildRequest(changeA);
        controller.handleBuildCommencing(new BuildCommencingEvent(this, createBuildResult(changeA), new PulseExecutionContext()));
        controller.handleBuildRequest(changeB);

        assertQueuedCount(7);
        assertActivatedCount(1);

        verify(buildRequestRegistry, times(1)).requestActivated(changeA, changeA.getId());

        controller.handleBuildCompleted(createSuccessful(changeA.getMetaBuildId(), utility));

        assertQueuedCount(4);
        assertActivatedCount(3);

        controller.handleBuildCompleted(createSuccessful(changeA.getMetaBuildId(), libraryA));

        assertQueuedCount(4);
        assertActivatedCount(2);

        controller.handleBuildCompleted(createSuccessful(changeA.getMetaBuildId(), libraryB));

        assertQueuedCount(3);
        assertActivatedCount(2);

        controller.handleBuildCompleted(createSuccessful(changeA.getMetaBuildId(), client));

        assertQueuedCount(3);
        assertActivatedCount(1);

        verify(buildRequestRegistry, never()).requestAssimilated((BuildRequestEvent) anyObject(), anyLong());
    }

    public void testBuildCancellationDueToFailureIsolatedToDependents()
    {
        Project a = createProject("A");
        Project b1 = createProject("B1", dependency(a));
        Project c1 = createProject("C1", dependency(b1));
        createProject("D1", dependency(c1));
        Project b2 = createProject("B2", dependency(a));
        Project c2 = createProject("C2", dependency(b2));

        BuildRequestEvent request = createRequest(a);
        controller.handleBuildRequest(request);

        assertQueuedCount(5);
        assertActivatedCount(1);

        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), a));

        assertQueuedCount(3);
        assertActivatedCount(2);

        controller.handleBuildCompleted(createFailed(request.getMetaBuildId(), b1));

        assertQueuedCount(1);
        assertActivatedCount(1);

        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), b2));

        assertQueuedCount(0);
        assertActivatedCount(1);

        controller.handleBuildCompleted(createSuccessful(request.getMetaBuildId(), c2));

        assertQueuedCount(0);
        assertActivatedCount(0);
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
        List<BuildRequestEvent> activatedEvents = newArrayList(transform(listener.getEventsReceived(BuildActivatedEvent.class), new Function<BuildActivatedEvent, BuildRequestEvent>()
        {
            public BuildRequestEvent apply(BuildActivatedEvent buildActivatedEvent)
            {
                return buildActivatedEvent.getEvent();
            }
        }));
        assertItemsSame(Arrays.asList(requests), activatedEvents);
    }
}
