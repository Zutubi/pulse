package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.STATUS_MILESTONE;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.TestUtils;
import static com.zutubi.pulse.master.model.Project.State.IDLE;
import static com.zutubi.pulse.master.tove.config.project.DependencyConfiguration.*;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

public class RebuildDependenciesAcceptanceTest extends AcceptanceTestBase
{
    private File tmpDir;
    private String projectName;
    private BuildRunner buildRunner;
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;

    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();

        Repository repository = new Repository();
        repository.clean();

        tmpDir = FileSystemUtils.createTempDir(randomName());

        projectName = randomName();

        buildRunner = new BuildRunner(rpcClient.RemoteApi);

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(rpcClient.RemoteApi);

        projects = new ProjectConfigurations(configurationHelper);
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();

        removeDirectory(tmpDir);

        super.tearDown();
    }

    private ProjectConfigurationHelper insertProject(ProjectConfigurationHelper project) throws Exception
    {
        configurationHelper.insertProject(project.getConfig(), false);
        return project;
    }

    public void testRebuildSingleDependency() throws Exception
    {
        WaitProject projectA = projects.createWaitAntProject(projectName + "A", tmpDir, false);
        insertProject(projectA);

        WaitProject projectB = projects.createWaitAntProject(projectName + "B", tmpDir, false);
        projectB.addDependency(projectA);
        insertProject(projectB);

        buildRunner.triggerRebuild(projectB.getConfig());

        // expect projectA to be building, projectB to be pending_dependency.
        rpcClient.RemoteApi.waitForBuildInProgress(projectA.getName(), 1);

        assertBuildQueued(projectB);

        projectA.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectA.getName(), 1);

        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectA.getName(), 1));

        // expect projectB to be building.
        rpcClient.RemoteApi.waitForBuildInProgress(projectB.getName(), 1);
        projectB.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectB.getName(), 1);

        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));
    }

    public void testRebuildMultipleDependencies() throws Exception
    {
        final WaitProject projectA = projects.createWaitAntProject(projectName + "A", tmpDir, false);
        insertProject(projectA);

        final WaitProject projectB = projects.createWaitAntProject(projectName + "B", tmpDir, false);
        insertProject(projectB);

        WaitProject projectC = projects.createWaitAntProject(projectName + "C", tmpDir, false);
        projectC.addDependency(projectA);
        projectC.addDependency(projectB);
        insertProject(projectC);

        buildRunner.triggerRebuild(projectC.getConfig());

        WaitProject firstDependency;
        WaitProject secondDependency;
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    return rpcClient.RemoteApi.getBuildStatus(projectA.getName(), 1) == ResultState.IN_PROGRESS || rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1) == ResultState.IN_PROGRESS;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        }, rpcClient.RemoteApi.BUILD_TIMEOUT, "a dependency to start building");

        if (rpcClient.RemoteApi.getBuildStatus(projectA.getName(), 1) == ResultState.IN_PROGRESS)
        {
            firstDependency = projectA;
            secondDependency = projectB;
        }
        else
        {
            firstDependency = projectB;
            secondDependency = projectA;
        }

        assertEquals(ResultState.PENDING, rpcClient.RemoteApi.getBuildStatus(secondDependency.getName(), 1));
        assertBuildQueued(projectC);
        firstDependency.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(firstDependency.getName(), 1);

        rpcClient.RemoteApi.waitForBuildInProgress(secondDependency.getName(), 1);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(firstDependency.getName(), 1));
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(secondDependency.getName(), 1));
        assertBuildQueued(projectC);
        secondDependency.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(secondDependency.getName(), 1);

        rpcClient.RemoteApi.waitForBuildInProgress(projectC.getName(), 1);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectA.getName(), 1));
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectC.getName(), 1));
        projectC.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectC.getName(), 1);
    }

    public void testRebuildTransitiveDependency() throws Exception
    {
        final WaitProject projectA = projects.createWaitAntProject(projectName + "A", tmpDir, false);
        insertProject(projectA);

        final WaitProject projectB = projects.createWaitAntProject(projectName + "B", tmpDir, false);
        projectB.addDependency(projectA);
        insertProject(projectB);

        final WaitProject projectC = projects.createWaitAntProject(projectName + "C", tmpDir, false);
        projectC.addDependency(projectB);
        insertProject(projectC);

        buildRunner.triggerRebuild(projectC.getConfig());

        rpcClient.RemoteApi.waitForBuildInProgress(projectA.getName(), 1);

        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectA.getName(), 1));
        assertBuildQueued(projectB);
        assertBuildQueued(projectC);

        projectA.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectA.getName(), 1);
        rpcClient.RemoteApi.waitForBuildInProgress(projectB.getName(), 1);

        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectA.getName(), 1));
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));
        assertBuildQueued(projectC);

        projectB.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectB.getName(), 1);
        rpcClient.RemoteApi.waitForBuildInProgress(projectC.getName(), 1);

        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectA.getName(), 1));
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectC.getName(), 1));

        projectC.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectC.getName(), 1);
    }

    public void testRebuildUsesTransitiveProperty() throws Exception
    {
        final ProjectConfigurationHelper projectA = projects.createTrivialAntProject(projectName + "A");
        insertProject(projectA);
        // even though project a is not rebuilt as part of the rebuild, project b does depend on it
        // so we require it to have been built at least once for project b to be successful.
        buildRunner.triggerSuccessfulBuild(projectA);

        final WaitProject projectB = projects.createWaitAntProject(projectName + "B", tmpDir, false);
        projectB.addDependency(projectA);
        insertProject(projectB);

        final WaitProject projectC = projects.createWaitAntProject(projectName + "C", tmpDir, false);
        projectC.addDependency(projectB).setTransitive(false);
        insertProject(projectC);

        buildRunner.triggerRebuild(projectC.getConfig());

        rpcClient.RemoteApi.waitForBuildInProgress(projectB.getName(), 1);

        assertEquals(IDLE, rpcClient.RemoteApi.getProjectState(projectA.getName()));
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));
        assertBuildQueued(projectC);

        projectB.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectB.getName(), 1);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));

        rpcClient.RemoteApi.waitForBuildInProgress(projectC.getName(), 1);

        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectC.getName(), 1));

        projectC.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectC.getName(), 1);
    }

    public void testRebuildUsesStatusProperty() throws Exception
    {
        final ProjectConfigurationHelper projectA = projects.createTrivialAntProject(projectName + "A");
        projectA.getConfig().getDependencies().setStatus(IvyStatus.STATUS_RELEASE);
        insertProject(projectA);
        buildRunner.triggerSuccessfulBuild(projectA);

        final WaitProject projectB = projects.createWaitAntProject(projectName + "B", tmpDir, false);
        projectB.addDependency(projectA).setRevision(REVISION_LATEST_RELEASE);
        insertProject(projectB);

        final WaitProject projectC = projects.createWaitAntProject(projectName + "C", tmpDir, false);
        projectC.addDependency(projectB).setRevision(REVISION_LATEST_MILESTONE);
        insertProject(projectC);

        final WaitProject projectD = projects.createWaitAntProject(projectName + "D", tmpDir, false);
        projectD.addDependency(projectC).setRevision(REVISION_LATEST_INTEGRATION);
        insertProject(projectD);

        buildRunner.triggerRebuild(projectD.getConfig(), asPair("status", (Object) STATUS_MILESTONE));

        rpcClient.RemoteApi.waitForBuildInProgress(projectB.getName(), 1);

        assertEquals(IDLE, rpcClient.RemoteApi.getProjectState(projectA.getName()));
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));
        assertBuildQueued(projectC);
        assertBuildQueued(projectD);

        projectB.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectB.getName(), 1);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));

        rpcClient.RemoteApi.waitForBuildInProgress(projectC.getName(), 1);
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectC.getName(), 1));
        assertBuildQueued(projectD);

        projectC.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectC.getName(), 1);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectC.getName(), 1));

        rpcClient.RemoteApi.waitForBuildInProgress(projectD.getName(), 1);
        assertEquals(ResultState.IN_PROGRESS, rpcClient.RemoteApi.getBuildStatus(projectD.getName(), 1));

        projectD.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectD.getName(), 1);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectD.getName(), 1));
    }

    public void testRebuildStopsOnFailure() throws Exception
    {
        ProjectConfigurationHelper projectA = projects.createFailAntProject(projectName + "A");
        insertProject(projectA);

        WaitProject projectB = projects.createWaitAntProject(projectName + "B", tmpDir, false);
        projectB.addDependency(projectA);
        insertProject(projectB);

        buildRunner.triggerRebuild(projectB.getConfig());

        rpcClient.RemoteApi.waitForBuildToComplete(projectA.getName(), 1);
        assertEquals(ResultState.FAILURE, rpcClient.RemoteApi.getBuildStatus(projectA.getName(), 1));

        assertNull(rpcClient.RemoteApi.getBuildStatus(projectB.getName(), 1));

        // We would normally have to release projectBs' build.  However, it did not run,
        // because projectA failed. 
    }

    public void testTransientArtifactDeliveryForMetaBuild() throws Exception
    {
        // require 2 agents for concurrent builds of project A and project B.
        rpcClient.RemoteApi.ensureAgent(AGENT_NAME);
        // Ensure that the agent is online and available for the build.  Starting the
        // build without the agent available will result in hung builds.
        rpcClient.RemoteApi.waitForAgentToBeIdle(AGENT_NAME);

        // project A -> project B -> project C
        DepAntProject projectA = projects.createDepAntProject(projectName + "A");
        projectA.addArtifacts("build/artifact.jar");
        projectA.addFilesToCreate("build/artifact.jar");
        projectA.clearTriggers();                   // do not want dependency trigger firing.
        projectA.getDefaultStage().setAgent(null);  // allow project A to run on any agent.
        insertProject(projectA);

        WaitProject projectB = projects.createWaitAntProject(projectName + "B", tmpDir, false);
        projectB.addDependency(projectA);
        projectB.clearTriggers();                   // do not want dependency trigger firing.
        insertProject(projectB);

        DepAntProject projectC = projects.createDepAntProject(projectName + "C");
        projectC.addDependency(projectB);
         // ensure we see the revision of the artifact being delivered
        projectC.getConfig().getDependencies().setRetrievalPattern("lib/[artifact]-[revision].[ext]");
        projectC.clearTriggers();                   // do not want dependency trigger firing.
        projectC.addExpectedFiles("lib/artifact-1.jar");
        projectC.addNotExpectedFiles("lib/artifact-2.jar");
        insertProject(projectC);

        buildRunner.triggerRebuild(projectC);

        rpcClient.RemoteApi.waitForBuildToComplete(projectA.getName(), 1);
        rpcClient.RemoteApi.waitForBuildInProgress(projectB.getName(), 1);

        buildRunner.triggerSuccessfulBuild(projectA);

        projectB.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(projectB.getName(), 1);
        rpcClient.RemoteApi.waitForBuildToComplete(projectC.getName(), 1);

        // ensure that project C uses project A build 1 artifacts.
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectC.getName(), 1));
    }

    private void assertBuildQueued(final ProjectConfigurationHelper project) throws Exception
    {
        assertNotNull(getQueuedBuild(project));
    }

    private Hashtable<String, Object> getQueuedBuild(final ProjectConfigurationHelper project)
            throws Exception
    {
        Vector<Hashtable<String, Object>> snapshot = rpcClient.RemoteApi.getBuildQueueSnapshot();
        return CollectionUtils.find(snapshot, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> build)
            {
                return build.get("project").equals(project.getName());
            }
        });
    }
}
