package com.zutubi.pulse.acceptance.cleanup;

import com.zutubi.pulse.acceptance.SeleniumTestBase;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.model.ProjectManager;

/**
 * The set of acceptance tests for the projects cleanup configuration.
 */
public class CleanupAcceptanceTest extends SeleniumTestBase
{
    private static final long BUILD_TIMEOUT = 90000;

    private CleanupTestUtils utils;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        xmlRpcHelper.loginAsAdmin();

        utils = new CleanupTestUtils(xmlRpcHelper, selenium, urls);
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();

        logout();

        super.tearDown();
    }

    public void testCleanupWorkingDirectories() throws Exception
    {
        String projectName = random;
        xmlRpcHelper.insertSimpleProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false);

        utils.setRetainWorkingCopy(projectName, true);
        utils.addCleanupRule(projectName, "working_directory", CleanupWhat.WORKING_DIRECTORIES_ONLY);

        xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(utils.hasBuildWorkingCopy(projectName, 1));
        assertTrue(utils.isBuildPresentViaUI(projectName, 1));

        xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(utils.isBuildPresentViaUI(projectName, 2));
        assertTrue(utils.hasBuildWorkingCopy(projectName, 2));

        assertFalse(utils.hasBuildWorkingCopy(projectName, 1));

        // verify that the UI is as expected - the working copy tab exists and displays the
        // appropriate messages.

        assertFalse(utils.isWorkingCopyPresentViaUI(projectName, 1));
        assertTrue(utils.isWorkingCopyPresentViaUI(projectName, 2));
    }

    public void testCleanupBuildArtifacts() throws Exception
    {
        String name = random;
        xmlRpcHelper.insertSimpleProject(name, ProjectManager.GLOBAL_PROJECT_NAME, false);

        utils.setRetainWorkingCopy(name, true);

        utils.addCleanupRule(name, "build_artifacts", CleanupWhat.BUILD_ARTIFACTS);

        xmlRpcHelper.runBuild(name, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(utils.hasBuildDirectory(name, 1));

        xmlRpcHelper.runBuild(name, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(utils.hasBuildDirectory(name, 2));

        assertTrue(utils.hasBuildDirectory(name, 1));
        assertTrue(utils.hasBuildWorkingCopy(name, 1));
        assertFalse(utils.hasBuildOutputDirectory(name, 1));
        assertFalse(utils.hasBuildFeaturesDirectory(name, 1));

        assertTrue(utils.isBuildPulseFilePresentViaUI(name, 1));
        assertTrue(utils.isBuildLogsPresentViaUI(name, 1));
        assertFalse(utils.isBuildArtifactsPresentViaUI(name, 1));
    }

    public void testCleanupAll() throws Exception
    {
        String projectName = random;
        xmlRpcHelper.insertSimpleProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false);

        utils.setRetainWorkingCopy(projectName, true);

        utils.addCleanupRule(projectName, "everything");

        xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(utils.hasBuild(projectName, 1));
        assertTrue(utils.isBuildPresentViaUI(projectName, 1));

        xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(utils.hasBuild(projectName, 2));
        assertTrue(utils.isBuildPresentViaUI(projectName, 2));

        assertFalse(utils.hasBuild(projectName, 1));
        assertFalse(utils.isBuildPresentViaUI(projectName, 1));
    }

    private void pause(int seconds)
    {
        try
        {
            Thread.sleep(com.zutubi.util.Constants.SECOND * seconds);
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }
}
