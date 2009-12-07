package com.zutubi.pulse.master.build.queue;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import static com.zutubi.pulse.master.model.Project.State;
import static com.zutubi.pulse.master.model.Project.Transition;
import com.zutubi.pulse.master.model.Sequence;
import com.zutubi.pulse.master.model.SequenceManager;
import static org.mockito.Mockito.*;

import java.util.List;

public class SchedulingControllerTest extends BaseQueueTestCase
{
    private static final Messages I18N = Messages.getInstance(SchedulingController.class);

    private SequenceManager sequenceManager;
    private SchedulingController controller;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

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

    // -- management of the project status.

    public void testProjectMarkedAsBuildingOnRequest()
    {
        BuildRequestEvent request = createRequest("a");
        controller.handleEvent(request);

        verify(projectManager, times(1)).makeStateTransition(request.getOwner().getId(), Transition.BUILDING);
    }

    public void testProjectMarkedAsIdleOnRequestCompletion() throws NoSuchFieldException
    {
        Project project = createProject("a");
        BuildRequestEvent request = createRequest(project);
        controller.handleEvent(request);
        verify(projectManager, times(1)).makeStateTransition(request.getOwner().getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, project);

        BuildCompletedEvent completed = createSuccessful(request);
        controller.handleEvent(completed);
        verify(projectManager, times(1)).makeStateTransition(request.getOwner().getId(), Transition.IDLE);
    }

    public void testProjectRemainsBuildingWhileRequestsQueued()
    {
        Project project = createProject("a");
        BuildRequestEvent requestA = createRequest("a");
        controller.handleEvent(requestA);
        verify(projectManager, times(1)).makeStateTransition(project.getId(), Transition.BUILDING);
        setProjectState(State.BUILDING, project);

        controller.handleEvent(createRequest("a"));
        controller.handleEvent(createRequest("a"));

        BuildCompletedEvent completed = createSuccessful(requestA);
        controller.handleEvent(completed);

        verify(projectManager, times(1)).makeStateTransition(anyLong(), (Project.Transition) anyObject());
    }

    public void testBuildingProjectStateUnchangedByPersonalBuild()
    {
        Project project = createProject("a", State.BUILDING);
        BuildRequestEvent request = createPersonalRequest(project);
        controller.handleEvent(request);

        assertActivated(request);
        verify(projectManager, times(0)).makeStateTransition(anyLong(), (Transition)anyObject());
    }

    public void testIdleProjectStateUnchangedByPersonalBuild()
    {
        Project project = createProject("a", State.IDLE);
        BuildRequestEvent request = createPersonalRequest(project);
        controller.handleEvent(request);

        assertActivated(request);
        verify(projectManager, times(0)).makeStateTransition(anyLong(), (Transition)anyObject());
    }

    public void testCompletionOfPersonalBuild()
    {
        Project project = createProject("a", State.IDLE);
        BuildRequestEvent request = createPersonalRequest(project);
        controller.handleEvent(request);

        assertActivated(request);
        verify(projectManager, times(0)).makeStateTransition(anyLong(), (Transition)anyObject());

        BuildCompletedEvent completed = createSuccessful(request);
        controller.handleEvent(completed);
        verify(projectManager, times(0)).makeStateTransition(anyLong(), (Transition)anyObject());
    }

    // -- requests are dropped when project is paused.

    public void testPausedProjectRequestsIgnored()
    {
        Project project = createProject("a", State.PAUSED);

        BuildRequestEvent request = createRequest(project);
        controller.handleEvent(request);

        assertActivated();
        assertQueued();

        verify(buildRequestRegistry, times(1)).requestRejected(request, I18N.format("rejected.project.state", State.PAUSED));
    }

    public void testPausedPersonalRequestIsActivated()
    {
        Project project = createProject("a", State.PAUSED);

        BuildRequestEvent request = createPersonalRequest(project);
        controller.handleEvent(request);

        assertActivated(request);
        assertQueued();
    }

    public void testDownstreamProjectPausedDoesNotImpactRequest()
    {
        Project projectA = createProject("a");
        Project projectB = createProject("b", State.PAUSED, dependency(projectA));
        BuildRequestEvent request = createRequest(projectA);
        controller.handleEvent(request);

        assertActivated(request);
        assertQueued();
    }

    public void testUpstreamProjectPausedHaltsRequest()
    {
        Project projectA = createProject("a", State.PAUSED);
        Project projectB = createProject("b", dependency(projectA));
        BuildRequestEvent request = createRebuildRequest(projectB);
        controller.handleEvent(request);

        assertActivated();
        assertQueued();

        verify(buildRequestRegistry, times(1)).requestRejected(request, I18N.format("rejected.related.project.state"));
    }

    public void testPausedProjectDependenciesDoNotQueue()
    {
        Project util = createProject("util");
        Project lib = createProject("lib", State.PAUSED, dependency(util));
        Project clientA = createProject("clientA", dependency(lib));
        Project clientB = createProject("clientB", dependency(lib));

        BuildRequestEvent request = createRequest(util);
        controller.handleEvent(request);

        assertActivated(request);
        assertQueued();

        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(buildRequestRegistry, times(1)).requestActivated(request, request.getId());
    }

    // -- build request status reporting

    public void testActivatedRequestRegistryTransitions()
    {
        BuildRequestEvent request = createRequest("a");
        controller.handleEvent(request);

        verify(buildRequestRegistry, times(1)).requestQueued(request);
        verify(buildRequestRegistry, times(1)).requestActivated(request, request.getId());
    }

    // -- build request to handler mapping
    

    // -- snapshot

    public void testActivatedRequestInSnapshot()
    {
        BuildRequestEvent request = createRequest("a");
        controller.handleEvent(request);

        assertActivated(request);
        assertQueued();
    }

    public void testQueuedRequestInSnapshot()
    {
        BuildRequestEvent requestA = createRequest("a");
        BuildRequestEvent requestB = createRequest("a");
        controller.handleEvent(requestA);
        controller.handleEvent(requestB);

        assertActivated(requestA);
        assertQueued(requestB);
    }

    public void testPersonalAndRegularBuildRequestsQueuedIndependently()
    {
        BuildRequestEvent requestA = createRequest("a");
        BuildRequestEvent requestB = createPersonalRequest("a");
        controller.handleEvent(requestA);
        controller.handleEvent(requestB);

        assertActivated(requestA, requestB);
    }

    // -- test cancellation
    public void testCancelActivatedRequestDoesNothing()
    {
        BuildRequestEvent requestA = createRequest("a");
        controller.handleEvent(requestA);

        assertActivated(requestA);

        controller.cancelRequest(requestA.getId());

        assertActivated(requestA);
    }

    public void testCancelQueuedRequest()
    {
        BuildRequestEvent requestA = createRequest("a");
        BuildRequestEvent requestB = createRequest("a");
        controller.handleEvent(requestA);
        controller.handleEvent(requestB);

        assertActivated(requestA);
        assertQueued(requestB);

        controller.cancelRequest(requestB.getId());

        assertActivated(requestA);
        assertQueued();
    }

    // -- utility methods..

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
}
