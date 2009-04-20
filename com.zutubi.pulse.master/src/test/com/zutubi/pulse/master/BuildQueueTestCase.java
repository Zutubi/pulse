package com.zutubi.pulse.master;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.BuildRevision;
import static com.zutubi.pulse.core.dependency.ivy.IvyManager.STATUS_INTEGRATION;
import com.zutubi.pulse.core.engine.PulseFileSource;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.MockScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.security.BuildTokenAuthenticationProvider;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.bean.WiringObjectFactory;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base helper test class for tests of the build queues, which share many
 * dependencies.
 */
public class BuildQueueTestCase extends PulseTestCase
{
    protected MasterConfigurationManager configurationManager;
    protected EventManager eventManager;
    protected ProjectManager projectManager;
    protected BuildManager buildManager;
    protected ThreadFactory threadFactory;
    protected WiringObjectFactory objectFactory;
    protected MasterLocationProvider masterLocationProvider;
    protected BuildHookManager buildHookManager;
    protected TestManager testManager;
    protected File tempDir;
    protected AtomicInteger nextId = new AtomicInteger(1);
    protected AccessManager accessManager;
    protected BuildTokenAuthenticationProvider buildTokenAuthenticationProvider;

    protected void setUp() throws Exception
    {
        super.setUp();

        configurationManager = mock(MasterConfigurationManager.class);
        doReturn(mock(MasterUserPaths.class)).when(configurationManager).getUserPaths();
        tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        doReturn(tempDir).when(configurationManager).getDataDirectory();

        projectManager = mock(ProjectManager.class);

        masterLocationProvider = mock(MasterLocationProvider.class);
        doReturn("mock location").when(masterLocationProvider).getMasterLocation();
        doReturn("mock url").when(masterLocationProvider).getMasterUrl();

        buildHookManager = mock(BuildHookManager.class);
        eventManager = new DefaultEventManager();
        buildManager = mock(BuildManager.class);
        threadFactory = new PulseThreadFactory();
        objectFactory = new WiringObjectFactory();
        testManager = mock(TestManager.class);
        accessManager = mock(AccessManager.class);
        buildTokenAuthenticationProvider = mock(BuildTokenAuthenticationProvider.class);
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
        super.tearDown();
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
        projectConfiguration.setProjectId(project.getId());
        project.setConfig(projectConfiguration);
        return project;
    }

    protected AbstractBuildRequestEvent createRequest(final Project owner, final long buildId, String source, boolean replaceable, Revision revision)
    {
        BuildRevision buildRevision = revision == null ? new BuildRevision() : new BuildRevision(revision, new PulseFileSource("pulse file"), false);
        TriggerOptions options = new TriggerOptions(null, source);
        options.setStatus(STATUS_INTEGRATION);
        options.setReplaceable(replaceable);
        return new AbstractBuildRequestEvent(BuildQueueTestCase.this, buildRevision, owner.getConfig(), options)
        {
            public Entity getOwner()
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
    }
}
