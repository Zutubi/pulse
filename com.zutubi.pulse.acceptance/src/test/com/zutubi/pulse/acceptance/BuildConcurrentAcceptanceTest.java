package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.Constants.WAIT_ANT_REPOSITORY;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.pages.browse.BuildChangesPage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BuildConcurrentAcceptanceTest extends AcceptanceTestBase
{
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private BuildRunner buildRunner;

    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tempDir = FileSystemUtils.createTempDir();

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(rpcClient.RemoteApi);

        projects = new ProjectConfigurations(configurationHelper);
        buildRunner = new BuildRunner(rpcClient.RemoteApi);
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();

        removeDirectory(tempDir);

        super.tearDown();
    }

    // Test that by default, builds run in the order they were triggered.
    public void testDefault() throws Exception
    {
        WaitProject project = createProject("A");
        bindStagesToMaster(project);
        insertProjects(project);

        buildRunner.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

        List<String> requestIds = buildRunner.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildRequestToBeHandled(requestIds.get(0));

        project.releaseBuild();
        rpcClient.RemoteApi.waitForProjectToBeIdle(project.getName());
    }

    public void testAllowTwoConcurrentBuilds() throws Exception
    {
        WaitProject project = createProject("A");
        bindStagesToMaster(project);
        project.getConfig().getOptions().setConcurrentBuilds(2);
        insertProjects(project);

        buildRunner.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

        buildRunner.triggerBuild(project);
         // pending means active, we only have one agent active atm.
        rpcClient.RemoteApi.waitForBuildInPending(project.getName(), 2);

        List<String> requestIds = buildRunner.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildRequestToBeHandled(requestIds.get(0));

        project.releaseBuild();
        rpcClient.RemoteApi.waitForProjectToBeIdle(project.getName());
    }

    public void testProjectConcurrentlyBuildingOnTwoAgents() throws Exception
    {
        WaitProject project = setUpConcurrentBuildsOnTwoAgents();
        project.releaseBuild();
        rpcClient.RemoteApi.waitForProjectToBeIdle(project.getName());
    }

    public void testActiveBuildThatIsNotLatest() throws Exception
    {
        WaitProject project = setUpConcurrentBuildsOnTwoAgents();

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        List<Long> buildIds = browsePage.getBuildIds(null, project.getName());
        assertEquals(2L, (long) buildIds.get(0));

        rpcClient.RemoteApi.cancelBuild(project.getName(), 2);
        rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 2);

        getBrowser().refresh();
        getBrowser().waitForPageToLoad(SeleniumBrowser.PAGELOAD_TIMEOUT);
        browsePage.waitFor();
        buildIds = browsePage.getBuildIds(null, project.getName());
        assertEquals(1L, (long) buildIds.get(0));

        project.releaseBuild();
        rpcClient.RemoteApi.waitForProjectToBeIdle(project.getName());
    }

    public void testReorderingWithinQueueCausingBackwardRevisions() throws Exception
    {
        WaitProject project = createProject("A");
        bindStagesToMaster(project);
        project.getConfig().getOptions().setConcurrentBuilds(3);
        insertProjects(project);

        buildRunner.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

        buildRunner.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildInPending(project.getName(), 2);

        buildRunner.triggerBuild(project, asPair("priority", (Object)"5"));
        rpcClient.RemoteApi.waitForBuildInPending(project.getName(), 3);

        // release to make build 3 active - terminate since we are unable to
        // selectively release a build at the moment
        rpcClient.RemoteApi.cancelBuild(project.getName(), 1);

        // update scm to change the revision.
        makeChangeToSvn();

        project.releaseBuild();
        rpcClient.RemoteApi.waitForProjectToBeIdle(project.getName());

        // view the changes tab and ensure that it does not blow up.  The data
        // is not as accurate as would be liked, but further changes to the
        // changelist handling are required before we can resolve this.

        getBrowser().loginAsAdmin();

        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, project.getName(), 3L);
        assertTrue(getBrowser().isTextPresent(changesPage.formatChangesSince(3)));
    }

    private void makeChangeToSvn() throws IOException, SVNException
    {
        File wcDir = createTempDirectory();
        SubversionWorkspace workspace = new SubversionWorkspace(wcDir, "pulse", "pulse");
        try
        {
            workspace.doCheckout(WAIT_ANT_REPOSITORY);

            File file = new File(wcDir, randomName() + "-file.txt");
            FileSystemUtils.copy(file, new File(wcDir, "build.xml"));

            workspace.doAdd(file);
            workspace.doCommit("new file", file);
        }
        finally
        {
            IOUtils.close(workspace);
            removeDirectory(wcDir);
        }
    }

    private WaitProject setUpConcurrentBuildsOnTwoAgents() throws Exception
    {
        rpcClient.RemoteApi.ensureAgent(AGENT_NAME);

        WaitProject project = createProject("A");
        bindStagesToAny(project);
        project.getConfig().getOptions().setConcurrentBuilds(2);
        insertProjects(project);

        buildRunner.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

        buildRunner.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 2);
        return project;
    }

    private WaitProject createProject(String suffix) throws Exception
    {
        return projects.createWaitAntProject(randomName() + "-" + suffix, new File(tempDir, suffix), false);
    }

    private void bindStagesToMaster(ProjectConfigurationHelper project) throws Exception
    {
        AgentConfiguration master = configurationHelper.getMasterAgentReference();
        for (BuildStageConfiguration stage : project.getStages())
        {
            stage.setAgent(master);
        }
    }

    private void bindStagesToAny(ProjectConfigurationHelper project)
    {
        for (BuildStageConfiguration stage : project.getStages())
        {
            stage.setAgent(null);
        }
    }

    private void insertProjects(ProjectConfigurationHelper... projects) throws Exception
    {
        for (ProjectConfigurationHelper project: projects)
        {
            configurationHelper.insertProject(project.getConfig(), false);
        }
    }
}