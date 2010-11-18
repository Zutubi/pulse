package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.TriggerBuildForm;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.zutubi.pulse.core.engine.api.ResultState.*;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Set of acceptance tests that work on testing builds priorities.
 */
public class BuildPriorityAcceptanceTest extends AcceptanceTestBase
{
    private static final int WAIT_FOR_TIMEOUT = 20000;

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
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);
        buildRunner = new BuildRunner(xmlRpcHelper);
        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.cancelIncompleteBuilds();
        xmlRpcHelper.logout();

        removeDirectory(tempDir);

        super.tearDown();
    }

    // Test that by default, builds run in the order they were triggered.
    public void testDefaultPriorities() throws Exception
    {
        WaitProject projectA = createProject("A");
        WaitProject projectB = createProject("B");
        WaitProject projectC = createProject("C");

        insertProjects(projectA, projectB, projectC);

        buildRunner.triggerBuild(projectA);
        xmlRpcHelper.waitForBuildInProgress(projectA.getName(), 1);
        
        buildRunner.triggerBuild(projectB);
        buildRunner.triggerBuild(projectC);

        assertBuildOrder(projectA, projectB, projectC);
    }

    public void testProjectPriorities() throws Exception
    {
        WaitProject projectA = createProject("A");
        WaitProject projectB = createProject("B");
        projectB.getOptions().setPriority(1);
        WaitProject projectC = createProject("C");
        projectC.getOptions().setPriority(5);

        insertProjects(projectA, projectB, projectC);

        buildRunner.triggerBuild(projectA);
        xmlRpcHelper.waitForBuildInProgress(projectA.getName(), 1);

        buildRunner.triggerBuild(projectB);
        buildRunner.triggerBuild(projectC);

        assertBuildOrder(projectA, projectC, projectB);
    }

    public void testNegativePriorityIsLowerThanDefaultPriority() throws Exception
    {
        WaitProject projectA = createProject("A");
        WaitProject projectB = createProject("B");
        projectB.getOptions().setPriority(-1);
        WaitProject projectC = createProject("C");

        insertProjects(projectA, projectB, projectC);

        buildRunner.triggerBuild(projectA);
        xmlRpcHelper.waitForBuildInProgress(projectA.getName(), 1);

        buildRunner.triggerBuild(projectB);
        buildRunner.triggerBuild(projectC);

        assertBuildOrder(projectA, projectC, projectB);
    }

    public void testStagePriorities() throws Exception
    {
        WaitProject project = projects.createWaitAntProject(randomName(), tempDir);
        project.addStage("B").setPriority(2);
        project.addStage("C").setPriority(4);
        project.addStage("D").setPriority(-1);
        bindStagesToMaster(project);

        insertProjects(project);

        buildRunner.triggerBuild(project);
        xmlRpcHelper.waitForBuildStageInProgress(project.getName(), project.getDefaultStage().getName(), 1, WAIT_FOR_TIMEOUT);

        assertEquals(IN_PROGRESS, xmlRpcHelper.getBuildStageStatus(project.getName(), project.getDefaultStage().getName(), 1));
        assertEquals(PENDING, xmlRpcHelper.getBuildStageStatus(project.getName(), "C", 1));
        assertEquals(PENDING, xmlRpcHelper.getBuildStageStatus(project.getName(), "B", 1));
        assertEquals(PENDING, xmlRpcHelper.getBuildStageStatus(project.getName(), "D", 1));

        project.releaseStage(project.getDefaultStage().getName());
        xmlRpcHelper.waitForBuildStageInProgress(project.getName(), "C", 1, WAIT_FOR_TIMEOUT);

        assertEquals(IN_PROGRESS, xmlRpcHelper.getBuildStageStatus(project.getName(), "C", 1));
        assertEquals(PENDING, xmlRpcHelper.getBuildStageStatus(project.getName(), "B", 1));
        assertEquals(PENDING, xmlRpcHelper.getBuildStageStatus(project.getName(), "D", 1));

        project.releaseStage("C");

        xmlRpcHelper.waitForBuildStageInProgress(project.getName(), "B", 1, WAIT_FOR_TIMEOUT);
        assertEquals(IN_PROGRESS, xmlRpcHelper.getBuildStageStatus(project.getName(), "B", 1));
        assertEquals(PENDING, xmlRpcHelper.getBuildStageStatus(project.getName(), "D", 1));

        project.releaseStage("B");

        xmlRpcHelper.waitForBuildStageInProgress(project.getName(), "D", 1, WAIT_FOR_TIMEOUT);
        assertEquals(IN_PROGRESS, xmlRpcHelper.getBuildStageStatus(project.getName(), "D", 1));

        project.releaseStage("D");

        xmlRpcHelper.waitForBuildToComplete(project.getName(), 1);
        xmlRpcHelper.waitForProjectToBeIdle(project.getName());
        assertEquals(SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testPriorityViaRemoteApi() throws Exception
    {
        WaitProject projectA = createProject("A");
        WaitProject projectB = createProject("B");
        WaitProject projectC = createProject("C");

        insertProjects(projectA, projectB, projectC);

        buildRunner.triggerBuild(projectA);
        xmlRpcHelper.waitForBuildInProgress(projectA.getName(), 1);

        buildRunner.triggerBuild(projectB, asPair("priority", (Object) 2));
        buildRunner.triggerBuild(projectC, asPair("priority", (Object) 3));

        assertBuildOrder(projectA, projectC, projectB);
    }

    public void testPriorityViaManualPrompt() throws Exception
    {
        WaitProject projectA = createProject("A");
        WaitProject projectB = createProject("B");
        WaitProject projectC = createProject("C");
        projectC.getOptions().setPrompt(true);
        
        insertProjects(projectA, projectB, projectC);

        buildRunner.triggerBuild(projectA);
        xmlRpcHelper.waitForBuildInProgress(projectA.getName(), 1);

        buildRunner.triggerBuild(projectB);

        getBrowser().loginAsAdmin();

        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, projectC.getName());
        home.triggerBuild();

        TriggerBuildForm form = getBrowser().createForm(TriggerBuildForm.class);
        form.waitFor();
        form.triggerFormElements(asPair("priority", "5"));

        assertBuildOrder(projectA, projectC, projectB);
    }

    public void testPriorityAcrossDependencies() throws Exception
    {
        WaitProject projectA = createProject("A");
        WaitProject projectB = createProject("B");
        WaitProject projectC = createProject("C");
        
        WaitProject projectD = createProject("D");
        WaitProject projectE = createProject("E");
        projectE.addDependency(projectD);

        insertProjects(projectA, projectB, projectC, projectD, projectE);

        buildRunner.triggerBuild(projectA);
        xmlRpcHelper.waitForBuildInProgress(projectA.getName(), 1);

        buildRunner.triggerBuild(projectB);
        buildRunner.triggerBuild(projectC);
        buildRunner.triggerBuild(projectD, asPair("priority", (Object)10));

        xmlRpcHelper.waitForBuildInPending(projectB.getName(), 1);
        xmlRpcHelper.waitForBuildInPending(projectC.getName(), 1);
        xmlRpcHelper.waitForBuildInPending(projectD.getName(), 1);

        projectA.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectA.getName(), 1);

        xmlRpcHelper.waitForBuildInProgress(projectD.getName(), 1);
        assertEquals(PENDING, xmlRpcHelper.getBuildStatus(projectB.getName(), 1));
        assertEquals(PENDING, xmlRpcHelper.getBuildStatus(projectC.getName(), 1));

        projectD.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectD.getName(), 1);

        // due to timing issues, project B ends up being triggered next because project D is
        // still in the build queue.
        xmlRpcHelper.waitForBuildInProgress(projectB.getName(), 1);
        assertEquals(PENDING, xmlRpcHelper.getBuildStatus(projectC.getName(), 1));
        xmlRpcHelper.waitForBuildInPending(projectE.getName(), 1);

        projectB.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        xmlRpcHelper.waitForBuildInProgress(projectE.getName(), 1);
        assertEquals(PENDING, xmlRpcHelper.getBuildStatus(projectC.getName(), 1));
        projectE.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectE.getName(), 1);

        xmlRpcHelper.waitForBuildInProgress(projectC.getName(), 1);
        projectC.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(projectC.getName(), 1);
    }

    private void assertBuildOrder(WaitProject... projects) throws Exception
    {
        List<WaitProject> remaining = new LinkedList<WaitProject>();
        remaining.addAll(Arrays.asList(projects));

        if (remaining.size() > 0)
        {
            do
            {
                WaitProject current = remaining.remove(0);
                xmlRpcHelper.waitForBuildInProgress(current.getName(), 1);
                for (WaitProject project : remaining)
                {
                    xmlRpcHelper.waitForBuildInPending(project.getName(), 1);
                }
                current.releaseBuild();
                xmlRpcHelper.waitForBuildToComplete(current.getName(), 1);
                assertEquals(SUCCESS, xmlRpcHelper.getBuildStatus(current.getName(), 1));
            }
            while (remaining.size() > 0);
        }
    }

    private WaitProject createProject(String suffix) throws Exception
    {
        WaitProject project = projects.createWaitAntProject(randomName() + "-" + suffix, new File(tempDir, suffix));
        // projects can only be built on the master to ensure that we get expected queing behaviour.
        bindStagesToMaster(project);
        return project;
    }

    private void bindStagesToMaster(WaitProject project) throws Exception
    {
        AgentConfiguration master = configurationHelper.getMasterAgentReference();
        for (BuildStageConfiguration stage : project.getStages())
        {
            stage.setAgent(master);
        }
    }


    private void insertProjects(WaitProject... projects) throws Exception
    {
        for (WaitProject project: projects)
        {
            configurationHelper.insertProject(project.getConfig(), false);
        }
    }
}
