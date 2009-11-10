package com.zutubi.pulse.master.build.queue;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.BuildRevision;
import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.*;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.MockScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.build.control.BuildControllerFactory;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.SingleBuildRequestEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import static com.zutubi.pulse.master.tove.config.project.DependencyConfiguration.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.bean.WiringObjectFactory;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// We have fields that looked unused but actually are by the magic WiringObjectFactory.
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
public class FatControllerTest extends PulseTestCase
{
    private FatController fatController;
    private AccessManager accessManager;
    private ProjectManager projectManager;
    private EventManager eventManager;
    private WiringObjectFactory objectFactory;
    private ThreadFactory threadFactory;
    private BuildControllerFactory buildControllerFactory;
    private BuildManager buildManager;

    private AtomicInteger nextProjectId = new AtomicInteger(1);
    private AtomicInteger nextBuildResultId = new AtomicInteger(1);
    private AtomicInteger nextHandle = new AtomicInteger(1);
    private AtomicLong nextBuildId = new AtomicLong(1);

    private SequenceManager sequenceManager;
    private BuildRequestRegistry buildRequestRegistry;

    // NOTE: This map can only store a single handler at a time, so only functions while a single
    // build per owner is possible.
    private Map<NamedEntity, BuildController> projectToControllerMap;
    private Map<NamedEntity, Long> projectToMetaBuildIdMap;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        accessManager = mock(AccessManager.class);
        projectManager = mock(ProjectManager.class);
        buildManager = mock(BuildManager.class);
        eventManager = new DefaultEventManager();
        objectFactory = new WiringObjectFactory();
        threadFactory = new PulseThreadFactory();
        
        buildControllerFactory = new BuildControllerFactory()
        {
            public BuildController create(final BuildRequestEvent request)
            {
                final long buildResultId = nextBuildResultId.getAndIncrement();
                
                BuildController handler = mock(BuildController.class);
                synchronized (FatControllerTest.this)
                {
                    stub(handler.getBuildResultId()).toReturn(buildResultId);

                    // a bit hackish, but prevents the need to implement the interface.
                    doAnswer(new Answer()
                    {
                        public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                        {
                            request.createResult(projectManager, buildManager);
                            return null;
                        }
                    }).when(handler).start();
                }

                // record enough information about the build request so that we can create
                // an appropriate build completed event 
                projectToControllerMap.put(request.getOwner(), handler);
                projectToMetaBuildIdMap.put(request.getOwner(), request.getMetaBuildId());

                return handler;
            }
        };

        Sequence buildIdSequence = new Sequence()
        {
            public long getNext()
            {
                return FatControllerTest.this.nextBuildId.getAndIncrement();
            }
        };
        sequenceManager = mock(SequenceManager.class);
        stub(sequenceManager.getSequence(FatController.SEQUENCE_BUILD_ID)).toReturn(buildIdSequence);

        buildRequestRegistry = new BuildRequestRegistry();
        buildRequestRegistry.setAccessManager(accessManager);

        objectFactory.initProperties(this);

        fatController = objectFactory.buildBean(FatController.class);
        fatController.init();
        fatController.enable();

        objectFactory.initProperties(this);

        projectToControllerMap = new HashMap<NamedEntity, BuildController>();
        projectToMetaBuildIdMap = new HashMap<NamedEntity, Long>();
    }

    //TODO: Testing to be done on the project state transitions that the FatController manages.

    public void testSingleSucceededRequest() throws InterruptedException
    {
        singleRequestHelper(ResultState.SUCCESS);
    }

    public void testSingleFailedRequest() throws InterruptedException
    {
        singleRequestHelper(ResultState.FAILURE);
    }

    public void testSingleErrorRequest() throws InterruptedException
    {
        singleRequestHelper(ResultState.ERROR);
    }

    private void singleRequestHelper(ResultState state) throws InterruptedException
    {
        Project project = createProject("project");
        SingleBuildRequestEvent request = createRequest(project);

        fatController.requestBuild(request);
        assertEquals(1, request.getMetaBuildId());

        assertCounts(project, 1, 0);

        publishCompletedEvent(project, state);
        assertCounts(project, 0, 0);
    }

    public void testConcurrentRequestForSingleProject() throws InterruptedException
    {
        Project project = createProject("project");
        SingleBuildRequestEvent request1 = createRequest(project);
        SingleBuildRequestEvent request2 = createRequest(project);

        fatController.requestBuild(request1);
        fatController.requestBuild(request2);

        assertEquals(1, request1.getMetaBuildId());
        assertEquals(2, request2.getMetaBuildId());
        assertCounts(project, 1, 1);

        publishSucceededEvent(project);
        assertCounts(project, 1, 0);

        publishSucceededEvent(project);
        assertCounts(project, 0, 0);
    }

    public void testConcurrentRequestForMultipleProjects() throws InterruptedException
    {
        Project project1 = createProject("project1");
        Project project2 = createProject("project2");

        SingleBuildRequestEvent request1 = createRequest(project1);
        SingleBuildRequestEvent request2 = createRequest(project2);

        fatController.requestBuild(request1);
        fatController.requestBuild(request2);

        assertCounts(project1, 1, 0);
        assertCounts(project2, 1, 0);

        publishSucceededEvent(project1);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 1, 0);

        publishSucceededEvent(project2);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 0, 0);
    }

    public void testSingleDependencySuccessfulRebuild() throws InterruptedException
    {
        Project project1 = createProject("project1");
        Project project2 = createProject("project2");

        addDependency(project1, project2);

        SingleBuildRequestEvent request = createRebuildRequest(project2);
        fatController.requestBuild(request);
        assertCounts(project1, 1, 0);
        assertCounts(project2, 0, 0);

        publishSucceededEvent(project1);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 1, 0);

        publishSucceededEvent(project2);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 0, 0);
    }

    public void testSingleDependencyFailedRebuild() throws InterruptedException
    {
        singleDependencyUnsuccessfulRebuildHelper(ResultState.FAILURE);
    }

    public void testSingleDependencyErroredRebuild() throws InterruptedException
    {
        singleDependencyUnsuccessfulRebuildHelper(ResultState.ERROR);
    }

    private void singleDependencyUnsuccessfulRebuildHelper(ResultState resultState) throws InterruptedException
    {
        Project project1 = createProject("project1");
        Project project2 = createProject("project2");

        addDependency(project1, project2);

        SingleBuildRequestEvent request = createRebuildRequest(project2);
        fatController.requestBuild(request);
        assertCounts(project1, 1, 0);
        assertCounts(project2, 0, 0);

        publishCompletedEvent(project1, resultState);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 0, 0);
    }

    public void testDependencyTreeCalculationUsesTheDependencyRevisionField() throws InterruptedException
    {
        dependencyTreeCalculationsUsesDependencyRevisionFieldHelper(REVISION_LATEST_MILESTONE, STATUS_INTEGRATION, false);
        dependencyTreeCalculationsUsesDependencyRevisionFieldHelper(REVISION_LATEST_INTEGRATION, STATUS_INTEGRATION, true);
        dependencyTreeCalculationsUsesDependencyRevisionFieldHelper(REVISION_LATEST_INTEGRATION, STATUS_MILESTONE, true);

        dependencyTreeCalculationsUsesDependencyRevisionFieldHelper(REVISION_CUSTOM, STATUS_INTEGRATION, false);
        dependencyTreeCalculationsUsesDependencyRevisionFieldHelper(REVISION_CUSTOM, STATUS_MILESTONE, false);
        dependencyTreeCalculationsUsesDependencyRevisionFieldHelper(REVISION_CUSTOM, STATUS_RELEASE, false);
    }

    private void dependencyTreeCalculationsUsesDependencyRevisionFieldHelper(String dependencyRevision, String requestStatus, boolean expectedTraversal) throws InterruptedException
    {
        Project project1 = createProject("project1");
        Project project2 = createProject("project2");

        addDependency(project1, project2).setRevision(dependencyRevision);

        dependencyTreeCalculationsHelper(project1, project2, requestStatus, expectedTraversal);
    }

    public void testDependencyTreeCalculationUsesTheDependencyTransitiveField() throws InterruptedException
    {
        Project project1 = createProject("project1");
        Project project2 = createProject("project2");

        addDependency(project1, project2).setTransitive(false);

        dependencyTreeCalculationsHelper(project1, project2, STATUS_INTEGRATION, false);
    }

    private void dependencyTreeCalculationsHelper(Project project1, Project project2, String requestStatus, boolean expectedTraversal) throws InterruptedException
    {
        SingleBuildRequestEvent request = createRebuildRequest(project2);
        request.getOptions().setStatus(requestStatus);

        fatController.requestBuild(request);

        if (expectedTraversal)
        {
            assertCounts(project1, 1, 0);
            assertCounts(project2, 0, 0);
            publishSucceededEvent(project1);
        }

        assertCounts(project1, 0, 0);
        assertCounts(project2, 1, 0);

        publishSucceededEvent(project2);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 0, 0);
    }

    public void testMultipleDependenciesRebuild() throws InterruptedException
    {
        Project project1 = createProject("project1-1");
        Project project2 = createProject("project1-2");
        Project project3= createProject("project1");

        addDependency(project1, project3);
        addDependency(project2, project3);

        SingleBuildRequestEvent rebuildRequest = createRebuildRequest(project3);
        fatController.requestBuild(rebuildRequest);
        assertCounts(project1, 1, 0);
        assertCounts(project2, 1, 0);
        assertCounts(project3, 0, 0);

        publishSucceededEvent(project1);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 1, 0);
        assertCounts(project3, 0, 0);

        publishSucceededEvent(project2);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 0, 0);
        assertCounts(project3, 1, 0);

        publishSucceededEvent(project3);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 0, 0);
        assertCounts(project3, 0, 0);
    }

    public void testDependencyChainRebuild() throws InterruptedException
    {
        Project project1 = createProject("project1-1-1");
        Project project2 = createProject("project1-1");
        Project project3= createProject("project1");

        addDependency(project1, project2);
        addDependency(project2, project3);

        SingleBuildRequestEvent rebuildRequest = createRebuildRequest(project3);
        fatController.requestBuild(rebuildRequest);
        assertCounts(project1, 1, 0);
        assertCounts(project2, 0, 0);
        assertCounts(project3, 0, 0);

        publishSucceededEvent(project1);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 1, 0);
        assertCounts(project3, 0, 0);

        publishSucceededEvent(project2);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 0, 0);
        assertCounts(project3, 1, 0);

        publishSucceededEvent(project3);
        assertCounts(project1, 0, 0);
        assertCounts(project2, 0, 0);
        assertCounts(project3, 0, 0);
    }

    /**
     * Add project1 as a dependency of project2.
     *
     * @param project1  the upstream project
     * @param project2  the downstream project.
     *
     * @return the dependency configuration instance.
     */
    private DependencyConfiguration addDependency(Project project1, Project project2)
    {
        return addDependency(project1, project2, REVISION_LATEST_INTEGRATION);
    }

    /**
     * Add project 1 as a dependency of project2, with the specific revision associated with the
     * dependency.
     *
     * @param project1  the upstream project
     * @param project2  the downstream project
     * @param revision  the dependency revision
     *
     * @return the dependency configuration instance.
     */
    private DependencyConfiguration addDependency(Project project1, Project project2, String revision)
    {
        DependencyConfiguration dependency = new DependencyConfiguration();
        dependency.setProject(project1.getConfig());
        dependency.setRevision(revision);
        dependency.setTransitive(true);
        project2.getConfig().getDependencies().getDependencies().add(dependency);
        return dependency;
    }

    private void assertCounts(Project project, int activeCount, int queuedCount)
    {
        assertActiveBuildCount(project, activeCount);
        assertQueuedBuildCount(project, queuedCount);
    }

    private void assertActiveBuildCount(Project project, int count)
    {
        BuildQueue.Snapshot snapshot = fatController.snapshotBuildQueue();
        List<EntityBuildQueue.ActiveBuild> build = snapshot.getActiveBuilds().get(project);
        assertEquals(count, (build != null) ? build.size() : 0);
    }

    private void assertQueuedBuildCount(Project project, int count)
    {
        BuildQueue.Snapshot snapshot = fatController.snapshotBuildQueue();
        List<BuildRequestEvent> requests = snapshot.getQueuedBuilds().get(project);
        assertEquals(count, (requests != null) ? requests.size() : 0);
    }

    private void publishSucceededEvent(Project project) throws InterruptedException
    {
        publishCompletedEvent(project, ResultState.SUCCESS);
    }

    private void publishFailedEvent(Project project) throws InterruptedException
    {
        publishCompletedEvent(project, ResultState.FAILURE);
    }

    private void publishErroredEvent(Project project) throws InterruptedException
    {
        publishCompletedEvent(project, ResultState.ERROR);
    }

    private void publishCompletedEvent(Project project, ResultState resultState) throws InterruptedException
    {
        long buildResultId = projectToControllerMap.get(project).getBuildResultId();
        long metaBuildId = projectToMetaBuildIdMap.get(project);

        BuildResult buildResult = new BuildResult(new UnknownBuildReason(), project, 0, false);
        buildResult.setId(buildResultId);
        buildResult.setMetaBuildId(metaBuildId);
        buildResult.setState(resultState);

        BuildCompletedEvent evt = new BuildCompletedEvent(this, buildResult, null);
        eventManager.publish(evt);

        // wait for the build to be finished by monitoring the active builds.
        while (isBuildActive(project, buildResultId))
        {
            Thread.yield();
        }

        assertNull(evt.getExceptions());
    }

    private boolean isBuildActive(Project project, final long buildResultId)
    {
        BuildQueue.Snapshot snapshot = fatController.snapshotBuildQueue();
        EntityBuildQueue.ActiveBuild activeBuild = CollectionUtils.find(snapshot.getActiveBuilds().get(project), new Predicate<EntityBuildQueue.ActiveBuild>()
        {
            public boolean satisfied(EntityBuildQueue.ActiveBuild activeBuild)
            {
                return activeBuild.getController().getBuildResultId() == buildResultId;
            }
        });
        return activeBuild != null;
    }

    private SingleBuildRequestEvent createRebuildRequest(Project project)
    {
        TriggerOptions options = createOptions();
        options.setRebuild(true);
        
        BuildRevision revision = createRevision("1");
        return new SingleBuildRequestEvent(this, project, revision, options);
    }

    private SingleBuildRequestEvent createRequest(Project project)
    {
        TriggerOptions options = createOptions();
        BuildRevision revision = createRevision("1");
        return new SingleBuildRequestEvent(this, project, revision, options);
    }

    private BuildRevision createRevision(String revision)
    {
        return new BuildRevision(new Revision(revision), false);
    }

    protected TriggerOptions createOptions()
    {
        return new TriggerOptions(new UnknownBuildReason(), "fat controller test");
    }

    protected Project createProject(String projectName)
    {
        Project project = new Project(Project.State.IDLE);
        project.setId(nextProjectId.getAndIncrement());
        ProjectConfiguration projectConfiguration = new ProjectConfiguration();
        projectConfiguration.setName(projectName);
        projectConfiguration.setScm(new MockScmConfiguration());
        projectConfiguration.setType(new CustomTypeConfiguration());
        projectConfiguration.setProjectId(project.getId());
        projectConfiguration.setHandle(nextHandle.getAndIncrement());
        project.setConfig(projectConfiguration);

        synchronized (this)
        {
            stub(projectManager.getProject(project.getId(), false)).toReturn(project);
        }

        return project;
    }
}
