package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.TriggerBuildForm;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.core.engine.api.ResultState;
import static com.zutubi.pulse.core.engine.api.ResultState.PENDING;
import static com.zutubi.pulse.core.engine.api.ResultState.IN_PROGRESS;
import static com.zutubi.pulse.core.engine.api.ResultState.SUCCESS;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.*;

/**
 * Set of acceptance tests that work on testing builds priorities.
 */
public class BuildPriorityAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private BuildRunner buildRunner;

    private List<String> createdProjects = new LinkedList<String>();
    
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
        removeDirectory(tempDir);

        // Cancel any builds that are still running so that they
        // do not block subsequent builds.

        for (Hashtable<String, Object> queuedRequest : xmlRpcHelper.getBuildQueueSnapshot())
        {
            xmlRpcHelper.cancelQueuedBuildRequest(queuedRequest.get("id").toString());
        }

        for (String projectName : createdProjects)
        {
            xmlRpcHelper.cancelBuild(projectName, 1);
        }

        xmlRpcHelper.logout();
                
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
        buildRunner.waitForBuildInProgress(projectA, 1);
        
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
        buildRunner.waitForBuildInProgress(projectA, 1);

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
        buildRunner.waitForBuildInProgress(projectA, 1);

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
        waitForStageInProgress(project, project.getDefaultStage().getName());

        assertEquals(IN_PROGRESS, getStageStatus(project, project.getDefaultStage().getName()));
        assertEquals(PENDING, getStageStatus(project, "C"));
        assertEquals(PENDING, getStageStatus(project, "B"));
        assertEquals(PENDING, getStageStatus(project, "D"));

        project.releaseStage(project.getDefaultStage().getName());
        waitForStageInProgress(project, "C");

        assertEquals(IN_PROGRESS, getStageStatus(project, "C"));
        assertEquals(PENDING, getStageStatus(project, "B"));
        assertEquals(PENDING, getStageStatus(project, "D"));

        project.releaseStage("C");

        waitForStageInProgress(project, "B");
        assertEquals(IN_PROGRESS, getStageStatus(project, "B"));
        assertEquals(PENDING, getStageStatus(project, "D"));

        project.releaseStage("B");

        waitForStageInProgress(project, "D");
        assertEquals(IN_PROGRESS, getStageStatus(project, "D"));

        project.releaseStage("D");

        buildRunner.waitForBuildToComplete(project, 1);
        assertEquals(SUCCESS, buildRunner.getBuildStatus(project, 1));
    }

    public void testPriorityViaRemoteApi() throws Exception
    {
        WaitProject projectA = createProject("A");
        WaitProject projectB = createProject("B");
        WaitProject projectC = createProject("C");

        insertProjects(projectA, projectB, projectC);

        buildRunner.triggerBuild(projectA);
        buildRunner.waitForBuildInProgress(projectA, 1);

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
        buildRunner.waitForBuildInProgress(projectA, 1);

        buildRunner.triggerBuild(projectB);

        // trigger build c via the manual prompt
        SeleniumBrowser browser = new SeleniumBrowser();
        try
        {
            browser.start();
            browser.loginAsAdmin();

            // trigger a build
            ProjectHomePage home = browser.openAndWaitFor(ProjectHomePage.class, projectC.getName());
            home.triggerBuild();

            TriggerBuildForm form = browser.createForm(TriggerBuildForm.class);
            form.waitFor();
            form.triggerFormElements(asPair("priority", "5"));
        }
        finally
        {
            browser.stop();
        }

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
        buildRunner.waitForBuildInProgress(projectA, 1);

        buildRunner.triggerBuild(projectB);
        buildRunner.triggerBuild(projectC);
        buildRunner.triggerBuild(projectD, asPair("priority", (Object)10));

        waitForBuildPending(projectB);
        waitForBuildPending(projectC);
        waitForBuildPending(projectD);

        projectA.releaseBuild();

        buildRunner.waitForBuildInProgress(projectD, 1);
        assertEquals(PENDING, buildRunner.getBuildStatus(projectB, 1));
        assertEquals(PENDING, buildRunner.getBuildStatus(projectC, 1));

        projectD.releaseBuild();

        // due to timing issues, project B ends up being triggered next because project D is
        // still in the build queue.
        buildRunner.waitForBuildInProgress(projectB, 1);
        assertEquals(PENDING, buildRunner.getBuildStatus(projectC, 1));
        waitForBuildPending(projectE);

        projectB.releaseBuild();

        buildRunner.waitForBuildInProgress(projectE, 1);
        assertEquals(PENDING, buildRunner.getBuildStatus(projectC, 1));
        projectE.releaseBuild();

        buildRunner.waitForBuildInProgress(projectC, 1);
        projectC.releaseBuild();
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
                buildRunner.waitForBuildInProgress(current, 1);
                for (WaitProject project : remaining)
                {
                    waitForBuildPending(project);
                }
                current.releaseBuild();
                buildRunner.waitForBuildToComplete(current, 1);
                assertEquals(SUCCESS, buildRunner.getBuildStatus(current, 1));
            }
            while (remaining.size() > 0);
        }
    }

    private WaitProject createProject(String suffix) throws Exception
    {
        // projects can only be built on the master to ensure that we get expected queing behaviour.
        WaitProject project = projects.createWaitAntProject(randomName() + "-" + suffix, new File(tempDir, suffix));
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
            createdProjects.add(project.getConfig().getName());
        }
    }

    private ResultState getStageStatus(WaitProject project, String stageName) throws Exception
    {
        Hashtable<String, Object> stage = getStage(project, stageName);
        if (stage != null)
        {
            return ResultState.fromPrettyString((String) stage.get("status"));
        }
        return null;
    }

    private Hashtable<String, Object> getStage(WaitProject project, final String stageName) throws Exception
    {
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project.getName(), 1);
        if (build != null)
        {
            Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
            return CollectionUtils.find(stages, new Predicate<Hashtable<String, Object>>()
            {
                public boolean satisfied(Hashtable<String, Object> stage)
                {
                    return stage.get("name").equals(stageName);
                }
            });
        }
        return null;
    }

    private void waitForBuildPending(final WaitProject project)
    {
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    Hashtable<String, Object> build = xmlRpcHelper.getBuild(project.getName(), 1);
                    return build != null && build.get("status").equals(PENDING.getPrettyString());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, 10000, "build of project " + project.getName() + " to be pending");

    }

    private void waitForStageInProgress(final WaitProject project, final String stageName)
    {
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    Hashtable<String, Object> stage = getStage(project, stageName);
                    return stage != null && stage.get("status").equals(IN_PROGRESS.getPrettyString());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, 10000, "stage " + stageName + " of project " + project.getName() + " to become in progress");
    }
}
