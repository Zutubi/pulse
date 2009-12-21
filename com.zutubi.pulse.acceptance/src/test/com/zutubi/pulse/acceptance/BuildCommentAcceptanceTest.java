package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.dependencies.ConfigurationHelper;
import com.zutubi.pulse.acceptance.dependencies.ProjectConfigurations;
import com.zutubi.pulse.acceptance.dependencies.WaitAntProject;
import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.acceptance.pages.browse.AddCommentDialog;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.BuildResult;
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

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.ensureProject(TEST_PROJECT);
        xmlRpcHelper.ensureUser(TEST_USER);
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testAddComment() throws Exception
    {
        addCommentHelper();
    }

    public void testCancelComment() throws Exception
    {
        int buildId = xmlRpcHelper.runBuild(TEST_PROJECT);

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
        xmlRpcHelper.insertTrivialUser(random);

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
        browser.waitForPageToLoad();
        assertTextNotPresent(TEST_COMMENT);
    }

    public void testAddCommentToInProgressBuild() throws Exception
    {
        // Checks that the build controller updating a build does not result
        // in comments being lost.
        File tempDir = FileSystemUtils.createTempDir(getName());
        try
        {
            ConfigurationHelper configurationHelper = new ConfigurationHelper();
            configurationHelper.setXmlRpcHelper(xmlRpcHelper);
            configurationHelper.init();

            ProjectConfigurations projects = new ProjectConfigurations(configurationHelper);
            WaitAntProject project = projects.createWaitAntProject(tempDir, random);
            project.getDefaultStage().setAgent(configurationHelper.getAgentReference(AgentManager.MASTER_AGENT_NAME));
            configurationHelper.insertProject(project.getConfig());
            xmlRpcHelper.waitForProjectToInitialise(project.getName());
            xmlRpcHelper.triggerBuild(project.getName());
            xmlRpcHelper.waitForBuildInProgress(project.getName(), 1);

            BuildSummaryPage page = addCommentToBuild(random, 1);

            project.releaseBuild();
            xmlRpcHelper.waitForBuildToComplete(random, 1);

            page.openAndWaitFor();
            assertTextPresent(TEST_COMMENT);
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    private BuildSummaryPage addCommentHelper() throws Exception
    {
        int buildId = xmlRpcHelper.runBuild(TEST_PROJECT);
        return addCommentToBuild(TEST_PROJECT, buildId);
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

        browser.waitForPageToLoad();
        assertTrue(page.isCommentsPresent());
        assertTextPresent(TEST_COMMENT);
        assertTextPresent("by " + TEST_USER);
        return page;
    }
}
