package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.acceptance.pages.browse.AddCommentDialog;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

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
            configurationHelper.insertProject(projects.createTrivialAntProject(TEST_PROJECT).getConfig(), false);
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

        assertTrue(browser.login(TEST_USER, ""));
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

        browser.logout();
        assertTrue(browser.login(random, ""));

        page.openAndWaitFor();
        assertFalse(page.isDeleteCommentLinkPresent(1));

        browser.logout();
        assertTrue(browser.login(TEST_USER, ""));
        
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
        WaitProject project = projects.createWaitAntProject(random, tempDir);
        configurationHelper.insertProject(project.getConfig(), false);

        int buildNumber = buildRunner.triggerBuild(project);
        buildRunner.waitForBuildInProgress(project, buildNumber);

        BuildSummaryPage page = addCommentToBuild(random, buildNumber);

        project.releaseBuild();

        buildRunner.waitForBuildToComplete(project, buildNumber);

        page.openAndWaitFor();
        assertTextPresent(TEST_COMMENT);
    }
    
    public void testRemoteApi() throws Exception
    {
        final String ANOTHER_TEST_COMMENT = "another comment";
        
        String user1 = random + "-user1";
        String user2 = random + "-user2";
        
        configurationHelper.insertUser(users.createSimpleUser(user1));
        configurationHelper.insertUser(users.createSimpleUser(user2));
        
        int buildNumber = buildRunner.triggerAndWaitForBuild(testProject);

        XmlRpcHelper user1Helper = new XmlRpcHelper();
        user1Helper.login(user1, "");
        XmlRpcHelper user2Helper = new XmlRpcHelper();
        user2Helper.login(user2, "");
        
        try
        {
            // Starts with no comments.
            Vector<Hashtable<String,Object>> comments = user1Helper.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(0, comments.size());
            
            // Add and verify a comment.
            String id1 = user1Helper.addBuildComment(TEST_PROJECT, buildNumber, TEST_COMMENT);
            assertTrue(Long.parseLong(id1) > 0);
            
            comments = user1Helper.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(1, comments.size());

            Hashtable<String, Object> comment = comments.get(0);
            assertEquals(user1, comment.get("author"));
            assertEquals(TEST_COMMENT, comment.get("message"));
            assertEquals(id1, comment.get("id"));

            // Add a second comment, verify again.
            String id2 = user1Helper.addBuildComment(TEST_PROJECT, buildNumber, ANOTHER_TEST_COMMENT);
            assertTrue(Long.parseLong(id2) > 0);
            
            comments = user1Helper.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(2, comments.size());
            assertEquals(TEST_COMMENT, comments.get(0).get("message"));
            assertEquals(ANOTHER_TEST_COMMENT, comments.get(1).get("message"));
            
            // Delete the second comment.
            assertTrue(user1Helper.deleteBuildComment(TEST_PROJECT, buildNumber, id2));
            comments = user1Helper.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(1, comments.size());
            assertEquals(id1, comments.get(0).get("id"));
            
            // Deleting an unknown comment just returns false.
            assertFalse(user1Helper.deleteBuildComment(TEST_PROJECT, buildNumber, id2));            
            
            // A second user should be able to see, but not delete, a comment.
            comments = user2Helper.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(1, comments.size());
            
            try
            {
                user2Helper.deleteBuildComment(TEST_PROJECT, buildNumber, id1);
                fail("Should not be able to delete another user's comments");
            }
            catch (Exception e)
            {
                assertThat(e.getMessage(), containsString("Permission to perform action 'delete' denied"));
            }
            
            // An admin can both see and delete any comments.
            comments = xmlRpcHelper.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(1, comments.size());
            assertTrue(xmlRpcHelper.deleteBuildComment(TEST_PROJECT, buildNumber, id1));
            comments = xmlRpcHelper.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(0, comments.size());
        }
        finally
        {
            user1Helper.logout();
            user2Helper.logout();
        }
    }

    private BuildSummaryPage addCommentHelper() throws Exception
    {
        int buildNumber = buildRunner.triggerAndWaitForBuild(testProject);
        return addCommentToBuild(testProject.getName(), buildNumber);
    }

    private BuildSummaryPage addCommentToBuild(String project, int buildId)
    {
        assertTrue(browser.login(TEST_USER, ""));
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
