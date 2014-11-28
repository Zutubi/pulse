package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.acceptance.pages.agents.CommentPage;
import com.zutubi.pulse.acceptance.pages.browse.AddCommentDialog;
import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.acceptance.utils.UserConfigurations;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.model.CommentContainer;
import com.zutubi.util.Condition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Helper base class for tests of comment pages.
 */
public abstract class CommentAcceptanceTestBase extends AcceptanceTestBase
{
    protected static final String TEST_USER = "comment-user";
    protected static final String TEST_COMMENT = "a comment here";

    protected UserConfigurations users;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();

        users = new UserConfigurations();
        if (!CONFIGURATION_HELPER.isUserExists(TEST_USER))
        {
            CONFIGURATION_HELPER.insertUser(users.createSimpleUser(TEST_USER));
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    protected void addCommentOnPage(CommentPage page)
    {
        getBrowser().loginAndWait(TEST_USER, "");
        page.openAndWaitFor();
        assertFalse(page.isCommentsPresent());

        page.clickAction(CommentContainer.ACTION_ADD_COMMENT);

        AddCommentDialog dialog = new AddCommentDialog(getBrowser());
        dialog.waitFor();
        dialog.typeInput(TEST_COMMENT);
        dialog.clickAffirm();

        page.waitForComments(SeleniumBrowser.WAITFOR_TIMEOUT);
        getBrowser().waitForTextPresent(TEST_COMMENT);
        getBrowser().waitForTextPresent("by " + TEST_USER);
    }

    protected void cancelCommentOnPage(CommentPage page)
    {
        getBrowser().loginAndWait(TEST_USER, "");
        page.openAndWaitFor();
        page.clickAction(CommentContainer.ACTION_ADD_COMMENT);

        AddCommentDialog dialog = new AddCommentDialog(getBrowser());
        dialog.waitFor();
        dialog.clickDecline();
        assertFalse(dialog.isVisible());
        assertFalse(page.isCommentsPresent());
    }

    protected void addAndDeleteCommentOnPage(final CommentPage page) throws Exception
    {
        CONFIGURATION_HELPER.insertUser(users.createSimpleUser(random));

        addCommentOnPage(page);
        final long commentId = getLatestCommentId();

        getBrowser().logout();
        getBrowser().loginAndWait(random, "");

        page.openAndWaitFor();
        assertTrue(page.isCommentPresent(commentId));
        assertFalse(page.isCommentDeleteLinkPresent(commentId));

        getBrowser().logout();
        getBrowser().loginAndWait(TEST_USER, "");

        page.openAndWaitFor();
        assertTrue(page.isCommentPresent(commentId));
        assertTrue(page.isCommentDeleteLinkPresent(commentId));
        ConfirmDialog confirmDialog = page.clickDeleteComment(commentId);
        confirmDialog.waitFor();
        confirmDialog.clickAffirm();
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !page.isCommentPresent(commentId);
            }
        }, SeleniumBrowser.WAITFOR_TIMEOUT, "comment to disappear");

    }

    protected void remoteApiHelper() throws Exception
    {
        final String ANOTHER_TEST_COMMENT = "another comment";

        String user1 = random + "-user1";
        String user2 = random + "-user2";

        CONFIGURATION_HELPER.insertUser(users.createSimpleUser(user1));
        CONFIGURATION_HELPER.insertUser(users.createSimpleUser(user2));


        RpcClient user1Client = new RpcClient();
        user1Client.login(user1, "");
        RpcClient user2Client = new RpcClient();
        user2Client.login(user2, "");

        try
        {
            // Starts with no comments.
            Vector<Hashtable<String,Object>> comments = getComments(user1Client);
            assertEquals(0, comments.size());

            // Add and verify a comment.
            String id1 = addComment(user1Client, TEST_COMMENT);
            assertTrue(Long.parseLong(id1) > 0);

            comments = getComments(user1Client);
            assertEquals(1, comments.size());

            Hashtable<String, Object> comment = comments.get(0);
            assertEquals(user1, comment.get("author"));
            assertEquals(TEST_COMMENT, comment.get("message"));
            assertEquals(id1, comment.get("id"));

            // Add a second comment, verify again.
            String id2 = addComment(user1Client, ANOTHER_TEST_COMMENT);
            assertTrue(Long.parseLong(id2) > 0);

            comments = getComments(user1Client);
            assertEquals(2, comments.size());
            assertEquals(TEST_COMMENT, comments.get(0).get("message"));
            assertEquals(ANOTHER_TEST_COMMENT, comments.get(1).get("message"));

            // Delete the second comment.
            assertTrue(deleteComment(user1Client, id2));
            comments = getComments(user1Client);
            assertEquals(1, comments.size());
            assertEquals(id1, comments.get(0).get("id"));

            // Deleting an unknown comment just returns false.
            assertFalse(deleteComment(user1Client, id2));

            // A second user should be able to see, but not delete, a comment.
            comments = getComments(user2Client);
            assertEquals(1, comments.size());

            try
            {
                deleteComment(user2Client, id1);
                fail("Should not be able to delete another user's comments");
            }
            catch (Exception e)
            {
                assertThat(e.getMessage(), containsString("Permission to perform action 'delete' denied"));
            }

            // An admin can both see and delete any comments.
            comments = getComments(rpcClient);
            assertEquals(1, comments.size());
            assertTrue(deleteComment(rpcClient, id1));
            comments = getComments(rpcClient);
            assertEquals(0, comments.size());
        }
        finally
        {
            user1Client.logout();
            user2Client.logout();
        }

    }

    protected abstract long getLatestCommentId() throws Exception;
    protected abstract Vector<Hashtable<String,Object>> getComments(RpcClient client) throws Exception;
    protected abstract String addComment(RpcClient client, String comment) throws Exception;
    protected abstract boolean deleteComment(RpcClient client, String id) throws Exception;
}
