package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.acceptance.pages.browse.AddCommentDialog;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Tests for displaying/adding/removing comments on builds.
 */
public class BuildCommentAcceptanceTest extends AcceptanceTestBase
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

        rpcClient.loginAsAdmin();

        buildRunner = new BuildRunner(rpcClient.RemoteApi);

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(rpcClient.RemoteApi);

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
        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();

        removeDirectory(tempDir);

        super.tearDown();
    }

    public void testAddComment() throws Exception
    {
        addCommentHelper();
    }

    public void testCancelComment() throws Exception
    {
        int buildId = buildRunner.triggerAndWaitForBuild(testProject);

        assertTrue(getBrowser().login(TEST_USER, ""));
        BuildSummaryPage page = getBrowser().openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, (long) buildId);
        page.clickAction(BuildResult.ACTION_ADD_COMMENT);

        AddCommentDialog dialog = new AddCommentDialog(getBrowser());
        dialog.waitFor();
        dialog.clickCancel();
        assertFalse(dialog.isVisible());
        assertFalse(page.isCommentsPresent());
    }

    public void testDeleteComment() throws Exception
    {
        configurationHelper.insertUser(users.createSimpleUser(random));

        final BuildSummaryPage page = addCommentHelper();
        final long commentId = getLatestCommentId(page.getBuildId());

        getBrowser().logout();
        assertTrue(getBrowser().login(random, ""));

        page.openAndWaitFor();
        assertTrue(page.isCommentPresent(commentId));
        assertFalse(page.isCommentDeleteLinkPresent(commentId));

        getBrowser().logout();
        assertTrue(getBrowser().login(TEST_USER, ""));
        
        page.openAndWaitFor();
        assertTrue(page.isCommentPresent(commentId));
        assertTrue(page.isCommentDeleteLinkPresent(commentId));
        ConfirmDialog confirmDialog = page.clickDeleteComment(commentId);
        confirmDialog.waitFor();
        confirmDialog.clickOk();
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !page.isCommentPresent(commentId);
            }
        }, SeleniumBrowser.WAITFOR_TIMEOUT, "comment to disappear");
    }

    // Checks that the build controller updating a build does not result in comments being lost.
    public void testAddCommentToInProgressBuild() throws Exception
    {
        WaitProject project = projects.createWaitAntProject(random, tempDir);
        configurationHelper.insertProject(project.getConfig(), false);

        List<String> requestIds = buildRunner.triggerBuild(project);
        Hashtable<String, Object> request = rpcClient.RemoteApi.waitForBuildRequestToBeActivated(requestIds.get(0));
        int buildNumber = Integer.valueOf(request.get("buildId").toString());

        BuildSummaryPage page = addCommentToBuild(random, buildNumber);

        project.releaseBuild();

        rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), buildNumber);

        page.openAndWaitFor();
        assertTrue(getBrowser().isTextPresent(TEST_COMMENT));
    }

    public void testRemoteApi() throws Exception
    {
        final String ANOTHER_TEST_COMMENT = "another comment";

        String user1 = random + "-user1";
        String user2 = random + "-user2";

        configurationHelper.insertUser(users.createSimpleUser(user1));
        configurationHelper.insertUser(users.createSimpleUser(user2));

        int buildNumber = buildRunner.triggerAndWaitForBuild(testProject);

        RpcClient user1Client = new RpcClient();
        user1Client.login(user1, "");
        RpcClient user2Client = new RpcClient();
        user2Client.login(user2, "");

        try
        {
            // Starts with no comments.
            Vector<Hashtable<String,Object>> comments = user1Client.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(0, comments.size());

            // Add and verify a comment.
            String id1 = user1Client.RemoteApi.addBuildComment(TEST_PROJECT, buildNumber, TEST_COMMENT);
            assertTrue(Long.parseLong(id1) > 0);

            comments = user1Client.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(1, comments.size());

            Hashtable<String, Object> comment = comments.get(0);
            assertEquals(user1, comment.get("author"));
            assertEquals(TEST_COMMENT, comment.get("message"));
            assertEquals(id1, comment.get("id"));

            // Add a second comment, verify again.
            String id2 = user1Client.RemoteApi.addBuildComment(TEST_PROJECT, buildNumber, ANOTHER_TEST_COMMENT);
            assertTrue(Long.parseLong(id2) > 0);

            comments = user1Client.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(2, comments.size());
            assertEquals(TEST_COMMENT, comments.get(0).get("message"));
            assertEquals(ANOTHER_TEST_COMMENT, comments.get(1).get("message"));

            // Delete the second comment.
            assertTrue(user1Client.RemoteApi.deleteBuildComment(TEST_PROJECT, buildNumber, id2));
            comments = user1Client.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(1, comments.size());
            assertEquals(id1, comments.get(0).get("id"));

            // Deleting an unknown comment just returns false.
            assertFalse(user1Client.RemoteApi.deleteBuildComment(TEST_PROJECT, buildNumber, id2));

            // A second user should be able to see, but not delete, a comment.
            comments = user2Client.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(1, comments.size());

            try
            {
                user2Client.RemoteApi.deleteBuildComment(TEST_PROJECT, buildNumber, id1);
                fail("Should not be able to delete another user's comments");
            }
            catch (Exception e)
            {
                assertThat(e.getMessage(), containsString("Permission to perform action 'delete' denied"));
            }

            // An admin can both see and delete any comments.
            comments = rpcClient.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(1, comments.size());
            assertTrue(rpcClient.RemoteApi.deleteBuildComment(TEST_PROJECT, buildNumber, id1));
            comments = rpcClient.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
            assertEquals(0, comments.size());
        }
        finally
        {
            user1Client.logout();
            user2Client.logout();
        }
    }

    private BuildSummaryPage addCommentHelper() throws Exception
    {
        int buildNumber = buildRunner.triggerAndWaitForBuild(testProject);
        return addCommentToBuild(testProject.getName(), buildNumber);
    }

    private BuildSummaryPage addCommentToBuild(String project, int buildId)
    {
        assertTrue(getBrowser().login(TEST_USER, ""));
        BuildSummaryPage page = getBrowser().openAndWaitFor(BuildSummaryPage.class, project, (long) buildId);
        assertFalse(page.isCommentsPresent());

        page.clickAction(BuildResult.ACTION_ADD_COMMENT);

        AddCommentDialog dialog = new AddCommentDialog(getBrowser());
        dialog.waitFor();
        dialog.typeInput(TEST_COMMENT);
        dialog.clickOk();

        page.waitForComments(SeleniumBrowser.WAITFOR_TIMEOUT);
        assertTrue(getBrowser().isTextPresent(TEST_COMMENT));
        assertTrue(getBrowser().isTextPresent("by " + TEST_USER));
        return page;
    }

    private long getLatestCommentId(long buildId) throws Exception
    {
        Vector<Hashtable<String, Object>> comments = rpcClient.RemoteApi.getBuildComments(TEST_PROJECT, (int) buildId);
        Hashtable<String, Object> latestComment = comments.get(comments.size() - 1);
        return Long.parseLong((String) latestComment.get("id"));
    }
}
