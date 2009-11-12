package com.zutubi.pulse.master;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourcePropertyConfiguration;
import com.zutubi.tove.security.AccessManager;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;

import java.util.Collections;

public class BuildRequestRegistryTest extends PulseTestCase
{
    private BuildRequestRegistry buildRequestRegistry;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        buildRequestRegistry = new BuildRequestRegistry();
        buildRequestRegistry.setAccessManager(mock(AccessManager.class));
        ProjectManager projectManager = mock(ProjectManager.class);
        Mockito.stub(projectManager.getProject(anyLong(), anyBoolean())).toReturn(new Project());
        buildRequestRegistry.setProjectManager(projectManager);
    }

    public void testReject()
    {
        final String MESSAGE = "reject message";

        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);
        assertStatus(BuildRequestRegistry.RequestStatus.UNHANDLED, event);

        buildRequestRegistry.requestRejected(event, MESSAGE);

        assertStatus(BuildRequestRegistry.RequestStatus.REJECTED, event);
        assertEquals(MESSAGE, buildRequestRegistry.getRejectionReason(event.getId()));
    }

    public void testAssimilate()
    {
        final long OTHER_ID = 22222;

        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        buildRequestRegistry.requestAssimilated(event, OTHER_ID);

        assertStatus(BuildRequestRegistry.RequestStatus.ASSIMILATED, event);
        assertEquals(OTHER_ID, buildRequestRegistry.getAssimilatedId(event.getId()));
    }

    public void testQueue()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        buildRequestRegistry.requestQueued(event);

        assertStatus(BuildRequestRegistry.RequestStatus.QUEUED, event);
    }

    public void testCancel()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);
        buildRequestRegistry.requestQueued(event);

        buildRequestRegistry.requestCancelled(event);

        assertStatus(BuildRequestRegistry.RequestStatus.CANCELLED, event);
    }

    public void testActivate()
    {
        final long BUILD = 1234;

        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);
        buildRequestRegistry.requestQueued(event);

        buildRequestRegistry.requestActivated(event, BUILD);

        assertStatus(BuildRequestRegistry.RequestStatus.ACTIVATED, event);
        assertEquals(BUILD, buildRequestRegistry.getBuildNumber(event.getId()));
    }

    public void testWaitForHandledRejected()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        assertEquals(BuildRequestRegistry.RequestStatus.UNHANDLED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1));
        buildRequestRegistry.requestRejected(event, "bing");
        assertEquals(BuildRequestRegistry.RequestStatus.REJECTED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1));
    }

    public void testWaitForHandledAssimilated()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        assertEquals(BuildRequestRegistry.RequestStatus.UNHANDLED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1));
        buildRequestRegistry.requestAssimilated(event, 1);
        assertEquals(BuildRequestRegistry.RequestStatus.ASSIMILATED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1));
    }

    public void testWaitForHandledQueued()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        assertEquals(BuildRequestRegistry.RequestStatus.UNHANDLED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1));
        buildRequestRegistry.requestQueued(event);
        assertEquals(BuildRequestRegistry.RequestStatus.QUEUED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1));
    }

    public void testWaitForHandledCancelled()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        assertEquals(BuildRequestRegistry.RequestStatus.UNHANDLED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1));
        buildRequestRegistry.requestCancelled(event);
        assertEquals(BuildRequestRegistry.RequestStatus.CANCELLED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1));
    }

    public void testWaitForHandledQueuedWhileWaiting()
    {
        final BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        runInBackgroundAfterPause(new Runnable()
        {
            public void run()
            {
                buildRequestRegistry.requestQueued(event);
            }
        });

        assertEquals(BuildRequestRegistry.RequestStatus.QUEUED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1000));
    }

    public void testWaitForHandledRejectedWhileWaiting()
    {
        final BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        runInBackgroundAfterPause(new Runnable()
        {
            public void run()
            {
                buildRequestRegistry.requestRejected(event, "oops");
            }
        });

        assertEquals(BuildRequestRegistry.RequestStatus.REJECTED, buildRequestRegistry.waitForRequestToBeHandled(event.getId(), 1000));
    }

    public void testWaitForActivatedRejected()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        assertEquals(BuildRequestRegistry.RequestStatus.UNHANDLED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1));
        buildRequestRegistry.requestRejected(event, "bing");
        assertEquals(BuildRequestRegistry.RequestStatus.REJECTED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1));
    }

    public void testWaitForActivatedAssimilated()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        assertEquals(BuildRequestRegistry.RequestStatus.UNHANDLED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1));
        buildRequestRegistry.requestAssimilated(event, 1);
        assertEquals(BuildRequestRegistry.RequestStatus.ASSIMILATED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1));
    }

    public void testWaitForActivatedQueued()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        assertEquals(BuildRequestRegistry.RequestStatus.UNHANDLED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1));
        buildRequestRegistry.requestQueued(event);
        assertEquals(BuildRequestRegistry.RequestStatus.QUEUED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1));
    }

    public void testWaitForActivatedCancelled()
    {
        BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        assertEquals(BuildRequestRegistry.RequestStatus.UNHANDLED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1));
        buildRequestRegistry.requestCancelled(event);
        assertEquals(BuildRequestRegistry.RequestStatus.CANCELLED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1));
    }

    public void testWaitForActivatedActivatedWhileWaiting()
    {
        final BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);
        buildRequestRegistry.requestQueued(event);

        runInBackgroundAfterPause(new Runnable()
        {
            public void run()
            {
                buildRequestRegistry.requestActivated(event, 1);
            }
        });

        assertEquals(BuildRequestRegistry.RequestStatus.ACTIVATED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1000));
    }

    public void testWaitForActivatedRejectedWhileWaiting()
    {
        final BuildRequestEvent event = createEvent();
        buildRequestRegistry.register(event);

        runInBackgroundAfterPause(new Runnable()
        {
            public void run()
            {
                buildRequestRegistry.requestRejected(event, "oops");
            }
        });

        assertEquals(BuildRequestRegistry.RequestStatus.REJECTED, buildRequestRegistry.waitForRequestToBeActivated(event.getId(), 1000));
    }

    public void testPruning()
    {
        buildRequestRegistry.setLimit(4);
        buildRequestRegistry.setTrim(2);

        BuildRequestEvent e1 = createEvent();
        BuildRequestEvent e2 = createEvent();
        BuildRequestEvent e3 = createEvent();
        BuildRequestEvent e4 = createEvent();
        BuildRequestEvent e5 = createEvent();
        BuildRequestEvent e6 = createEvent();
        BuildRequestEvent e7 = createEvent();

        buildRequestRegistry.register(e1);
        buildRequestRegistry.register(e2);
        buildRequestRegistry.register(e3);
        buildRequestRegistry.register(e4);

        assertKnown(e1, e2, e3, e4);

        buildRequestRegistry.register(e5);

        assertKnown(e3, e4, e5);
        assertUnknown(e1, e2);

        buildRequestRegistry.register(e6);

        assertKnown(e3, e4, e5, e6);
        assertUnknown(e1, e2);

        buildRequestRegistry.register(e7);

        assertKnown(e5, e6, e7);
        assertUnknown(e1, e2, e3, e4);
    }

    private void assertKnown(AbstractBuildRequestEvent... requests)
    {
        for (AbstractBuildRequestEvent abe: requests)
        {
            assertTrue(buildRequestRegistry.getStatus(abe.getId()) != BuildRequestRegistry.RequestStatus.UNKNOWN);
        }
    }

    private void assertUnknown(AbstractBuildRequestEvent... requests)
    {
        for (AbstractBuildRequestEvent abe: requests)
        {
            assertEquals(BuildRequestRegistry.RequestStatus.UNKNOWN, buildRequestRegistry.getStatus(abe.getId()));
        }
    }

    private void runInBackgroundAfterPause(final Runnable delegate)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                delegate.run();
            }
        }).start();
    }

    private void assertStatus(BuildRequestRegistry.RequestStatus expectedStatus, BuildRequestEvent event)
    {
        assertEquals(expectedStatus, buildRequestRegistry.getStatus(event.getId()));
    }

    private BuildRequestEvent createEvent()
    {
        Project project = new Project();
        project.setConfig(new ProjectConfiguration("test"));
        return new BuildRequestEvent(this, null, project, Collections.<ResourcePropertyConfiguration>emptyList(), null, "source", true);
    }
}
