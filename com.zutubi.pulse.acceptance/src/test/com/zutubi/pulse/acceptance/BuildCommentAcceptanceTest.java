package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.acceptance.utils.BuildRunner;
import com.zutubi.pulse.acceptance.utils.WaitProject;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Tests for displaying/adding/removing comments on builds.
 */
public class BuildCommentAcceptanceTest extends CommentAcceptanceTestBase
{
    private static final String TEST_PROJECT = "comment-test-project";

    private BuildRunner buildRunner;
    private File tempDir;
    private ProjectConfiguration testProject;
    private int buildNumber;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tempDir = FileSystemUtils.createTempDir(getName());

        rpcClient.loginAsAdmin();

        buildRunner = new BuildRunner(rpcClient.RemoteApi);

        if (!CONFIGURATION_HELPER.isProjectExists(TEST_PROJECT))
        {
            CONFIGURATION_HELPER.insertProject(projectConfigurations.createTrivialAntProject(TEST_PROJECT).getConfig(), false);
        }

        testProject = CONFIGURATION_HELPER.getConfigurationReference("projects/" + TEST_PROJECT, ProjectConfiguration.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.cancelIncompleteBuilds();

        removeDirectory(tempDir);

        super.tearDown();
    }

    public void testAddComment() throws Exception
    {
        buildNumber = buildRunner.triggerAndWaitForBuild(testProject);
        BuildSummaryPage page = getBrowser().createPage(BuildSummaryPage.class, testProject.getName(), (long) buildNumber);
        addCommentOnPage(page);
    }

    public void testCancelComment() throws Exception
    {
        buildNumber = buildRunner.triggerAndWaitForBuild(testProject);
        BuildSummaryPage page = getBrowser().createPage(BuildSummaryPage.class, TEST_PROJECT, (long) buildNumber);
        cancelCommentOnPage(page);
    }

    public void testDeleteComment() throws Exception
    {
        buildNumber = buildRunner.triggerAndWaitForBuild(testProject);
        BuildSummaryPage page = getBrowser().createPage(BuildSummaryPage.class, TEST_PROJECT, (long) buildNumber);
        addAndDeleteCommentOnPage(page);
    }

    // Checks that the build controller updating a build does not result in comments being lost.
    public void testAddCommentToInProgressBuild() throws Exception
    {
        WaitProject project = projectConfigurations.createWaitAntProject(random, tempDir, false);
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

        List<String> requestIds = buildRunner.triggerBuild(project);
        Hashtable<String, Object> request = rpcClient.RemoteApi.waitForBuildRequestToBeActivated(requestIds.get(0));
        int buildNumber = Integer.valueOf(request.get("buildId").toString());

        BuildSummaryPage page = getBrowser().createPage(BuildSummaryPage.class, random, (long) buildNumber);
        addCommentOnPage(page);

        project.releaseBuild();

        rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), buildNumber);

        page.openAndWaitFor();
        getBrowser().waitForTextPresent(TEST_COMMENT);
    }

    public void testRemoteApi() throws Exception
    {
        buildNumber = buildRunner.triggerAndWaitForBuild(testProject);
        remoteApiHelper();
    }

    protected long getLatestCommentId() throws Exception
    {
        Vector<Hashtable<String, Object>> comments = rpcClient.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
        Hashtable<String, Object> latestComment = comments.get(comments.size() - 1);
        return Long.parseLong((String) latestComment.get("id"));
    }

    protected Vector<Hashtable<String,Object>> getComments(RpcClient client) throws Exception
    {
        return client.RemoteApi.getBuildComments(TEST_PROJECT, buildNumber);
    }

    protected String addComment(RpcClient client, String comment) throws Exception
    {
        return client.RemoteApi.addBuildComment(TEST_PROJECT, buildNumber, comment);
    }

    protected boolean deleteComment(RpcClient client, String id) throws Exception
    {
        return client.RemoteApi.deleteBuildComment(TEST_PROJECT, buildNumber, id);
    }
}
