package com.zutubi.pulse.master;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.BuildRevision;
import static com.zutubi.pulse.core.dependency.ivy.IvyManager.STATUS_INTEGRATION;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.MockScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
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
    protected BuildHandlerFactory buildHandlerFactory;

    protected Map<AbstractBuildRequestEvent, BuildHandler> handlers = new HashMap<AbstractBuildRequestEvent, BuildHandler>();

    protected void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager();
        objectFactory = new WiringObjectFactory();
        buildHandlerFactory = mock(BuildHandlerFactory.class);
        accessManager = mock(AccessManager.class);
    }

    protected void assertActive(List<EntityBuildQueue.ActiveBuild> activeSnapshot, AbstractBuildRequestEvent... events)
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
                assertSame(events[i], activeSnapshot.get(i).getEvent());
            }
        }
    }

    protected void assertQueued(List<AbstractBuildRequestEvent> queuedSnapshot, AbstractBuildRequestEvent... events)
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
                assertSame(events[i], queuedSnapshot.get(i));
            }
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

    protected AbstractBuildRequestEvent createRequest(final Project owner, final long buildId, String source, boolean replaceable, Revision revision)
    {
        BuildRevision buildRevision = revision == null ? new BuildRevision() : new BuildRevision(revision, false);
        TriggerOptions options = new TriggerOptions(null, source);
        options.setStatus(STATUS_INTEGRATION);
        options.setReplaceable(replaceable);
        AbstractBuildRequestEvent request = new AbstractBuildRequestEvent(BuildQueueTestCase.this, buildRevision, owner.getConfig(), options)
        {
            public NamedEntity getOwner()
            {
                return owner;
            }

            public boolean isPersonal()
            {
                return false;
            }

            public BuildResult createResult(ProjectManager projectManager, UserManager userManager)
            {
                BuildResult buildResult = new BuildResult(new UnknownBuildReason(), owner, 0, false);
                buildResult.setId(buildId);
                buildResult.setStatus(options.getStatus());
                return buildResult;
            }
        };

        // By default, the build handler that is used by the BuildQueue to process the build request does nothing.
        BuildHandler handler = mock(BuildHandler.class);
        doReturn(handler).when(buildHandlerFactory).createHandler(request);
        doReturn(buildId).when(handler).getBuildId();

        handlers.put(request, handler);
        
        return request;
    }
}
