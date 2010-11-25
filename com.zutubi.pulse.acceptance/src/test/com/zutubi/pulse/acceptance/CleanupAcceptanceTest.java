package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.acceptance.utils.CleanupTestUtils;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.Condition;
import static com.zutubi.util.Constants.SECOND;
import com.zutubi.util.RandomUtils;

import java.util.Hashtable;
import java.util.Vector;

/**
 * The set of acceptance tests for the projects cleanup configuration.
 */
public class CleanupAcceptanceTest extends AcceptanceTestBase
{
    private static final long CLEANUP_TIMEOUT = SECOND * 10;

    private CleanupTestUtils utils;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        getBrowser().loginAsAdmin();
        rpcClient.loginAsAdmin();

        utils = new CleanupTestUtils(rpcClient.RemoteApi);

        Hashtable<String, Object> antConfig = rpcClient.RemoteApi.getAntConfig();
        antConfig.put(Constants.Project.AntCommand.TARGETS, "build");
        rpcClient.RemoteApi.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), antConfig);
        utils.deleteCleanupRule(random, "default");
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();

        getBrowser().logout();

        super.tearDown();
    }

    public void testCleanupBuildArtifacts() throws Exception
    {
        final String projectName = random;

        utils.addCleanupRule(projectName, "build_artifacts", CleanupWhat.BUILD_ARTIFACTS);

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously();

        assertTrue(utils.hasBuildDirectory(projectName, 1));

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously(new InvertedCondition()
        {
            public boolean notSatisfied() throws Exception
            {
                return utils.hasBuildOutputDirectory(projectName, 1);
            }
        }, new InvertedCondition()
        {
            public boolean notSatisfied() throws Exception
            {
                return utils.hasBuildFeaturesDirectory(projectName, 1);
            }
        });

        assertTrue(utils.hasBuildDirectory(projectName, 2));

        assertTrue(utils.hasBuildDirectory(projectName, 1));
        assertFalse(utils.hasBuildOutputDirectory(projectName, 1));
        assertFalse(utils.hasBuildFeaturesDirectory(projectName, 1));

        assertTrue(isBuildPulseFilePresentViaUI(projectName, 1));
        assertBuildLogsPresentViaUI(projectName, 1);
        assertStageLogsPresentViaUI(projectName, 1, "default");
        assertFalse(isBuildArtifactsPresentViaUI(projectName, 1));

        // the remote api returns artifacts based on what is in the database.
        Vector artifactsInBuild = rpcClient.RemoteApi.getArtifactsInBuild(projectName, 1);
        assertEquals(3, artifactsInBuild.size());
    }

    public void testCleanupAll() throws Exception
    {
        final String projectName = random;

        utils.addCleanupRule(projectName, "everything");

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously();

        assertTrue(utils.hasBuild(projectName, 1));
        assertTrue(isBuildPresentViaUI(projectName, 1));

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously(new InvertedCondition()
        {
            public boolean notSatisfied() throws Exception
            {
                return utils.hasBuild(projectName, 1);
            }
        });

        assertTrue(utils.hasBuild(projectName, 2));
        assertTrue(isBuildPresentViaUI(projectName, 2));

        assertFalse(utils.hasBuild(projectName, 1));
        assertFalse(isBuildPresentViaUI(projectName, 1));

        // Unknown build '1' for project 'testCleanupAll-8KHqy3jjGJ'
        try
        {
            rpcClient.RemoteApi.getArtifactsInBuild(projectName, 1);
        }
        catch(Exception e)
        {
            assertTrue(e.getMessage().contains("Unknown build '1' for project '"+projectName+"'"));
        }
    }

    public void testTestCleanup() throws Exception
    {
        final String projectName = random;

        utils.setAntTarget(projectName, "test");
        utils.insertTestCapture("projects/" + projectName, "junit xml report processor");
        utils.addCleanupRule(projectName, "build_artifacts", CleanupWhat.BUILD_ARTIFACTS);

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously();

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously(new InvertedCondition()
        {
            public boolean notSatisfied() throws Exception
            {
                return utils.hasBuildOutputDirectory(projectName, 1);
            }
        }, new InvertedCondition()
        {
            public boolean notSatisfied() throws Exception
            {
                return utils.hasBuildFeaturesDirectory(projectName, 1);
            }
        });

        // build 2 shows tests
        BuildTestsPage buildTests = getBrowser().openAndWaitFor(BuildTestsPage.class, projectName, 2L);
        assertTrue(buildTests.hasFailedTests());
        assertFalse(buildTests.isFailureUnavailableMessageShown());

        // build 1 does not show tests.
        buildTests = getBrowser().openAndWaitFor(BuildTestsPage.class, projectName, 1L);
        assertTrue(buildTests.hasFailedTests());
        assertTrue(buildTests.isFailureUnavailableMessageShown());

        StageTestsPage stageTests = getBrowser().openAndWaitFor(StageTestsPage.class, projectName, 2L, "default");
        assertFalse(stageTests.isLoadFailureMessageShown());
        assertTrue(stageTests.isBreadcrumbsVisible());

        stageTests = getBrowser().openAndWaitFor(StageTestsPage.class, projectName, 1L, "default");
        assertTrue(stageTests.isLoadFailureMessageShown());
        assertTrue(stageTests.isBreadcrumbsVisible());
    }

    public void testCleanupRepositoryArtifacts() throws Exception
    {
        final String projectName = random;
        prepareProjectWithRepositoryCleanup(projectName);

        rpcClient.RemoteApi.runBuild(projectName);
        assertTrue(utils.hasIvyFile(projectName, 2));

        waitForCleanupToRunAsynchronously(new InvertedCondition()
        {
            public boolean notSatisfied() throws Exception
            {
                return utils.hasIvyFile(projectName, 1);
            }
        });

        assertFalse(utils.hasIvyFile(projectName, 1));
    }

    public void testCleanupRepositoryArtifactsAfterProjectRename() throws Exception
    {
        final String projectName = random;
        prepareProjectWithRepositoryCleanup(projectName);

        // rename the project.
        String newProjectName = renameProject(projectName);
        rpcClient.RemoteApi.runBuild(newProjectName);

        waitForCleanupToRunAsynchronously(new InvertedCondition()
        {
            public boolean notSatisfied() throws Exception
            {
                return utils.hasIvyFile(projectName, 1);
            }
        });

        assertTrue(utils.hasIvyFile(newProjectName, 2));
        assertFalse(utils.hasIvyFile(projectName, 1));
    }

    private void prepareProjectWithRepositoryCleanup(String projectName) throws Exception
    {
        utils.addCleanupRule(projectName, "repository_artifacts", CleanupWhat.REPOSITORY_ARTIFACTS);

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously();

        assertTrue(utils.hasIvyFile(projectName, 1));
    }

    public void testCleanupLogs() throws Exception
    {
        final String projectName = random;

        utils.addCleanupRule(projectName, "logs", CleanupWhat.LOGS);

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously();

        assertTrue(utils.hasBuildLog(projectName, 1));
        assertBuildLogsPresentViaUI(projectName, 1);
        assertStageLogsPresentViaUI(projectName, 1, "default");

        rpcClient.RemoteApi.runBuild(projectName);
        waitForCleanupToRunAsynchronously(new InvertedCondition()
        {
            public boolean notSatisfied() throws Exception
            {
                return utils.hasBuildLog(projectName, 1);
            }
        });

        assertTrue(isBuildPresentViaUI(projectName, 2));
        assertBuildLogsPresentViaUI(projectName, 2);
        assertStageLogsPresentViaUI(projectName, 2, "default");
        assertTrue(utils.hasBuildLog(projectName, 2));

        assertTrue(isBuildPresentViaUI(projectName, 1));
        assertBuildLogsNotPresentViaUI(projectName, 1);
        assertStageLogsNotPresentViaUI(projectName, 1, "default");
        assertFalse(utils.hasBuildLog(projectName, 1));
    }

    private String renameProject(String projectName) throws Exception
    {
        String newProjectName = getName() + "-" + RandomUtils.randomString(10);
        Hashtable<String, Object> projectConfig = rpcClient.RemoteApi.getConfig("projects/" + projectName);
        projectConfig.put("name", newProjectName);
        rpcClient.RemoteApi.saveConfig("projects/" + projectName, projectConfig, false);
        return newProjectName;
    }

    private void waitForCleanupToRunAsynchronously(Condition... conditions)
    {
        if (conditions.length > 0)
        {
            int i = 0;
            for (Condition c : conditions)
            {
                i++;
                TestUtils.waitForCondition(c, CLEANUP_TIMEOUT, "condition("+i+") to be satisfied.");
            }
        }
        else
        {
            try
            {
                Thread.sleep(CLEANUP_TIMEOUT);
            }
            catch (InterruptedException e)
            {
                // noop.
            }
        }
    }

    public boolean isBuildPresentViaUI(String projectName, long buildNumber)
    {
        BuildSummaryPage page = getBrowser().open(BuildSummaryPage.class, projectName, buildNumber);
        getBrowser().waitForPageToLoad();
        if (getBrowser().isTextPresent("Unknown build"))
        {
            return false;
        }
        else
        {
            page.waitFor();
            return true;
        }
    }

    public boolean isBuildPulseFilePresentViaUI(String projectName, long buildNumber)
    {
        BuildFilePage page = getBrowser().createPage(BuildFilePage.class, projectName, buildNumber);
        return canOpenPage(page);
    }

    public void assertBuildLogsPresentViaUI(String projectName, long buildNumber)
    {
        BuildLogPage buildLog = openBuildLogsUI(projectName, buildNumber);
        assertTrue(buildLog.isLogAvailable());
    }

    public void assertStageLogsPresentViaUI(String projectName, long buildNumber, String stageName)
    {
        StageLogPage stageLog = openStageLogsUI(projectName, buildNumber, stageName);
        assertTrue(stageLog.isLogAvailable());
    }

    public void assertBuildLogsNotPresentViaUI(String projectName, long buildNumber)
    {
        BuildLogPage buildLog = openBuildLogsUI(projectName, buildNumber);
        assertTrue(buildLog.isLogNotAvailable());
    }

    public void assertStageLogsNotPresentViaUI(String projectName, long buildNumber, String stageName)
    {
        StageLogPage stageLog = openStageLogsUI(projectName, buildNumber, stageName);
        assertTrue(stageLog.isLogNotAvailable());
    }

    private BuildLogPage openBuildLogsUI(String projectName, long buildNumber)
    {
        BuildLogPage page = getBrowser().createPage(BuildLogPage.class, projectName, buildNumber);
        if (!canOpenPage(page))
        {
            fail("Failed to open build log page.");
        }
        return page;
    }

    private StageLogPage openStageLogsUI(String projectName, long buildNumber, String stageName)
    {
        StageLogPage page = getBrowser().createPage(StageLogPage.class, projectName, buildNumber, stageName);
        if (!canOpenPage(page))
        {
            fail("Failed to open stage log page.");
        }
        return page;
    }

    private boolean isBuildArtifactsPresentViaUI(String projectName, long buildNumber)
    {
        BuildArtifactsPage page = getBrowser().openAndWaitFor(BuildArtifactsPage.class, projectName, buildNumber);

        // reset the filter to ensure that what we need is available.
        page.resetFilter();

        // if artifacts are available, we should have the build command open in the tree.
        getBrowser().waitForLocator(page.getArtifactLocator("environment"));
        return page.isArtifactAvailable("environment");
    }

    private boolean canOpenPage(SeleniumPage page)
    {
        page.open();
        getBrowser().waitForPageToLoad();
        return page.isPresent();
    }

    private abstract class InvertedCondition implements Condition
    {
        public boolean satisfied()
        {
            try
            {
                return !notSatisfied();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        public abstract boolean notSatisfied() throws Exception;
    }
}
