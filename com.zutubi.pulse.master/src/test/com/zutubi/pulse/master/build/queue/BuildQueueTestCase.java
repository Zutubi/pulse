package com.zutubi.pulse.master.build.queue;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.BuildRevision;
import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.STATUS_INTEGRATION;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.MockScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.build.control.BuildControllerFactory;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.bean.WiringObjectFactory;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base helper test class for tests of the build queues, which share many
 * dependencies.
 */
public abstract class BuildQueueTestCase extends PulseTestCase
{
    protected EventManager eventManager;
    protected WiringObjectFactory objectFactory;
    protected AtomicInteger nextId = new AtomicInteger(1);
    protected AccessManager accessManager;
    protected BuildControllerFactory buildControllerFactory;
    protected BuildRequestRegistry buildRequestRegistry;

    protected Map<BuildRequestEvent, BuildController> controllers = new HashMap<BuildRequestEvent, BuildController>();

    protected void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager();
        objectFactory = new WiringObjectFactory();
        buildControllerFactory = mock(BuildControllerFactory.class);
        accessManager = mock(AccessManager.class);

        buildRequestRegistry = new BuildRequestRegistry();
        buildRequestRegistry.setAccessManager(accessManager);
    }

    protected void assertActive(List<EntityBuildQueue.ActiveBuild> activeSnapshot, BuildRequestEvent... events)
    {
        if (events.length == 0)
        {
            if (activeSnapshot != null)
            {
                assertEquals(0, activeSnapshot.size());
            }
        }
        else
        {
            assertEquals(events.length, activeSnapshot.size());
            for (int i = 0; i < events.length; i++)
            {
                BuildRequestEvent event = events[i];
                assertSame(event, activeSnapshot.get(i).getEvent());
                assertEquals(BuildRequestRegistry.RequestStatus.ACTIVATED, buildRequestRegistry.getStatus(event.getId()));
            }
        }
    }

    protected void assertQueued(List<BuildRequestEvent> queuedSnapshot, BuildRequestEvent... events)
    {
        if (events.length == 0)
        {
            if (queuedSnapshot != null)
            {
                assertEquals(0, queuedSnapshot.size());
            }
        }
        else
        {
            assertEquals(events.length, queuedSnapshot.size());
            for (int i = 0; i < events.length; i++)
            {
                BuildRequestEvent event = events[i];
                assertSame(event, queuedSnapshot.get(i));
                assertEquals(BuildRequestRegistry.RequestStatus.QUEUED, buildRequestRegistry.getStatus(event.getId()));
            }
        }
    }

    protected void assertRejected(BuildRequestEvent... events)
    {
        assertStatus(BuildRequestRegistry.RequestStatus.REJECTED, events);
    }

    protected void assertAssimilated(BuildRequestEvent... events)
    {
        assertStatus(BuildRequestRegistry.RequestStatus.ASSIMILATED, events);
    }

    protected void assertCancelled(BuildRequestEvent... events)
    {
        assertStatus(BuildRequestRegistry.RequestStatus.CANCELLED, events);
    }

    private void assertStatus(BuildRequestRegistry.RequestStatus expectedStatus, BuildRequestEvent... events)
    {
        for (BuildRequestEvent event: events)
        {
            assertEquals(expectedStatus, buildRequestRegistry.getStatus(event.getId()));
        }
    }

    protected Project createProject(String projectName)
    {
        Project project = new Project();
        project.setId(nextId.getAndIncrement());
        ProjectConfiguration projectConfiguration = new ProjectConfiguration();
        projectConfiguration.setName(projectName);
        projectConfiguration.setScm(new MockScmConfiguration());
        projectConfiguration.setType(new CustomTypeConfiguration());
        projectConfiguration.setProjectId(project.getId());
        project.setConfig(projectConfiguration);
        return project;
    }

    protected BuildRequestEvent createRequest(final Project owner, final long buildId, String source, boolean replaceable, Revision revision)
    {
        BuildRevision buildRevision = revision == null ? new BuildRevision() : new BuildRevision(revision, false);
        TriggerOptions options = new TriggerOptions(null, source);
        options.setStatus(STATUS_INTEGRATION);
        options.setReplaceable(replaceable);
        BuildRequestEvent request = new BuildRequestEvent(BuildQueueTestCase.this, buildRevision, owner.getConfig(), options)
        {
            public NamedEntity getOwner()
            {
                return owner;
            }

            public boolean isPersonal()
            {
                return false;
            }

            public String getStatus()
            {
                return options.getStatus();
            }

            public BuildResult createResult(ProjectManager projectManager, BuildManager buildManager)
            {
                BuildResult buildResult = new BuildResult(new UnknownBuildReason(), owner, 0, false);
                buildResult.setId(buildId);
                buildResult.setStatus(getStatus());
                return buildResult;
            }
        };

        // By default, the build handler that is used by the BuildQueue to process the build request does nothing.
        BuildController controller = mock(BuildController.class);
        doReturn(controller).when(buildControllerFactory).create(request);
        doReturn(buildId).when(controller).getBuildResultId();

        controllers.put(request, controller);

        buildRequestRegistry.register(request);
        return request;
    }
}
