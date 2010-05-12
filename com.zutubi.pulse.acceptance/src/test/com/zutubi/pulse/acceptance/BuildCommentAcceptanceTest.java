package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.acceptance.pages.browse.AddCommentDialog;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.FileSystemUtils;

import java.io.File;

/**
 * Tests for displaying/adding/removing comments on builds.
 */
public class BuildCommentAcceptanceTest extends SeleniumTestBase
{
    private static final String TEST_PROJECT = "comment-test-project";
    private static final String TEST_USER = "comment-user";
    private static final String TEST_COMMENT = "a comment here";

    private BuildRunner buildRunner;
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private File tempDir;
    private UserConfigurations users;
    private ProjectConfiguration testProject;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tempDir = FileSystemUtils.createTempDir(getName());

        xmlRpcHelper.loginAsAdmin();

        buildRunner = new BuildRunner(xmlRpcHelper);

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);
        if (!configurationHelper.isProjectExists(TEST_PROJECT))
        {
            configurationHelper.insertProject(projects.createTrivialAntProject(TEST_PROJECT).getConfig());
        }

        users = new UserConfigurations();
        if (!configurationHelper.isUserExists(TEST_USER))
        {
            configurationHelper.insertUser(users.createSimpleUser(TEST_USER));
        }

        testProject = configurationHelper.getConfigurationReference("projects/" + TEST_PROJECT, ProjectConfiguration.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);

        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testAddComment() throws Exception
    {
        addCommentHelper();
    }

    public void testCancelComment() throws Exception
    {
        int buildId = buildRunner.triggerAndWaitForBuild(testProject);

        login(TEST_USER, "");
        BuildSummaryPage page = browser.openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, (long) buildId);
        page.clickAction(BuildResult.ACTION_ADD_COMMENT);

        AddCommentDialog dialog = new AddCommentDialog(browser);
        dialog.waitFor();
        dialog.clickCancel();
        assertFalse(dialog.isVisible());
        assertFalse(page.isCommentsPresent());
    }

    public void testDeleteComment() throws Exception
    {
        configurationHelper.insertUser(users.createSimpleUser(random));

        BuildSummaryPage page = addCommentHelper();

        logout();
        login(random, "");

        page.openAndWaitFor();
        assertFalse(page.isDeleteCommentLinkPresent(1));

        logout();
        login(TEST_USER, "");
        
        page.openAndWaitFor();
        assertTrue(page.isDeleteCommentLinkPresent(1));
        ConfirmDialog confirmDialog = page.clickDeleteComment(1);
        confirmDialog.waitFor();
        confirmDialog.clickOk();
        page.waitForReload();
        assertTextNotPresent(TEST_COMMENT);
    }

    // Checks that the build controller updating a build does not result in comments being lost.
    public void testAddCommentToInProgressBuild() throws Exception
    {
        WaitProject project = projects.createWaitAntProject(tempDir, random);
        configurationHelper.insertProject(project.getConfig());

        int buildNumber = buildRunner.triggerBuild(project);
        buildRunner.waitForBuildInProgress(project, buildNumber);

        BuildSummaryPage page = addCommentToBuild(random, buildNumber);

        project.releaseBuild();

        buildRunner.waitForBuildToComplete(project, buildNumber);

        page.openAndWaitFor();
        assertTextPresent(TEST_COMMENT);
    }

    private BuildSummaryPage addCommentHelper() throws Exception
    {
        int buildNumber = buildRunner.triggerAndWaitForBuild(testProject);
        return addCommentToBuild(testProject.getName(), buildNumber);
    }

    private BuildSummaryPage addCommentToBuild(String project, int buildId)
    {
        login(TEST_USER, "");
        BuildSummaryPage page = browser.openAndWaitFor(BuildSummaryPage.class, project, (long) buildId);
        assertFalse(page.isCommentsPresent());

        page.clickAction(BuildResult.ACTION_ADD_COMMENT);

        AddCommentDialog dialog = new AddCommentDialog(browser);
        dialog.waitFor();
        dialog.typeInput(TEST_COMMENT);
        dialog.clickOk();

        page.waitForReload();
        assertTrue(page.isCommentsPresent());
        assertTextPresent(TEST_COMMENT);
        assertTextPresent("by " + TEST_USER);
        return page;
    }
}
