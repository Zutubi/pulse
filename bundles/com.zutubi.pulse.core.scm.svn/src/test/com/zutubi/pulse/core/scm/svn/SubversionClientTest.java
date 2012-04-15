package com.zutubi.pulse.core.scm.svn;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.junit.IOAssertions;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

public class SubversionClientTest extends PulseTestCase
{
    private static final String REPO = "svn://localhost/test/";
    private static final String USER = "jsankey";
    private static final String PASSWORD = "password";

    private static final String TRUNK_PATH = REPO + "trunk";
    private static final String BRANCH_PATH = REPO + "branches/new-branch";
    private static final String TAG_PATH = REPO + "tags/test-tag";
    
    private static final int REVISION_TRUNK_LATEST = 4;

    private SubversionClient client;
    private File tmpDir;
    private File gotDir;
    private File expectedDir;
    private Process serverProcess;
    private PulseExecutionContext context;
    private SVNClientManager clientManager;

//    $ svn log -v svn://localhost/test
//    ------------------------------------------------------------------------
//    r8 | jsankey | 2006-06-20 18:26:41 +1000 (Tue, 20 Jun 2006) | 1 line
//    Changed paths:
//       A /test/tags
//
//    Make tags dir
//    ------------------------------------------------------------------------
//    r7 | jsankey | 2006-06-20 17:32:58 +1000 (Tue, 20 Jun 2006) | 1 line
//    Changed paths:
//       M /test/branches/dev-it/afolder/f1
//
//    Edit a branch
//    ------------------------------------------------------------------------
//    r6 | jsankey | 2006-06-20 17:31:51 +1000 (Tue, 20 Jun 2006) | 1 line
//    Changed paths:
//       A /test/branches/dev-it (from /test/trunk:5)
//
//    Make a branch
//    ------------------------------------------------------------------------
//    r5 | jsankey | 2006-06-20 17:31:48 +1000 (Tue, 20 Jun 2006) | 1 line
//    Changed paths:
//       A /test/branches
//
//    Make a dir
//    ------------------------------------------------------------------------
//    r4 | jsankey | 2006-06-20 17:30:28 +1000 (Tue, 20 Jun 2006) | 1 line
//    Changed paths:
//       D /test/trunk/bar
//
//    Delete a file
//    ------------------------------------------------------------------------
//    r3 | jsankey | 2006-06-20 17:30:17 +1000 (Tue, 20 Jun 2006) | 1 line
//    Changed paths:
//       A /test/trunk/bar
//
//    Add a file
//    ------------------------------------------------------------------------
//    r2 | jsankey | 2006-06-20 17:30:00 +1000 (Tue, 20 Jun 2006) | 1 line
//    Changed paths:
//       M /test/trunk/foo
//
//    Edit a file
//    ------------------------------------------------------------------------
//    r1 | jsankey | 2006-06-20 16:13:29 +1000 (Tue, 20 Jun 2006) | 1 line
//    Changed paths:
//       A /test
//       A /test/trunk
//       A /test/trunk/afolder
//       A /test/trunk/afolder/f1
//       A /test/trunk/afolder/f2
//       A /test/trunk/bfolder
//       A /test/trunk/bfolder/f1
//       A /test/trunk/foo
//
//    Importing test data
//    ------------------------------------------------------------------------

    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDir(getClass().getName(), "");
        File repoDir = new File(tmpDir, "repo");
        FileSystemUtils.createDirectory(repoDir);

        expectedDir = new File(repoDir, "expected");
        FileSystemUtils.createDirectory(expectedDir);

        gotDir = new File(repoDir, "got");
        FileSystemUtils.createDirectory(gotDir);

        context = new PulseExecutionContext();
        context.setWorkingDir(gotDir);

        unzipInput("data", repoDir);
        serverProcess = Runtime.getRuntime().exec(new String[]{"svnserve", "--foreground", "--listen-port=3690", "--listen-host=127.0.0.1", "-dr", "."}, null, repoDir);

        TestUtils.waitForServer(3690);

        clientManager = SVNClientManager.newInstance(null, SVNWCUtil.createDefaultAuthenticationManager(USER, PASSWORD));

        createSubversionClient(TRUNK_PATH);
    }

    protected void tearDown() throws Exception
    {
        IOUtils.close(client);
        serverProcess.destroy();
        serverProcess.waitFor();
        clientManager.dispose();
        
        removeDirectory(tmpDir);
        super.tearDown();
    }

    public void testGetLatestRevision() throws ScmException
    {
        client = createSubversionClient("svn://localhost/");
        assertEquals("8", client.getLatestRevision(null).getRevisionString());
    }

    public void testGetLatestRevisionRestrictedToFiles() throws ScmException
    {
        assertEquals("4", client.getLatestRevision(null).getRevisionString());
    }

    public void testGetLatestRevisionNewBranch() throws ScmException, SVNException
    {
        SVNCopyClient client = clientManager.getCopyClient();
        SVNCopySource[] copySource = {new SVNCopySource(SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNURL.parseURIDecoded(TRUNK_PATH))};
        SVNCommitInfo info = client.doCopy(copySource, SVNURL.parseURIDecoded(BRANCH_PATH), false, true, true, "Create a branch", null);

        SubversionClient branchClient = new SubversionClient(BRANCH_PATH, false);
        assertEquals(Long.toString(info.getNewRevision()), branchClient.getLatestRevision(null).getRevisionString());
    }

    public void testGetLatestRevisionBadRepository()
    {
        try
        {
            client = createSubversionClient("svn://localhost/no/such/repo");
            client.getLatestRevision(null);
            fail();
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().contains("no repository found"));
        }
    }

    public void testList() throws ScmException
    {
        List<ScmFile> files = client.browse(null, "afolder", null);
        assertEquals(2, files.size());
        assertEquals("f1", files.get(0).getName());
        assertEquals("f2", files.get(1).getName());
    }

    public void testListNonExistent() throws ScmException
    {
        try
        {
            client.browse(null, "nosuchfile", null);
            fail();
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    public void testTag() throws ScmException, IOException
    {
        client.tag(null, createRevision(1), TAG_PATH, false);

        SubversionClient confirmServer = null;

        try
        {
            confirmServer = new SubversionClient(TAG_PATH, false, USER, PASSWORD);
            List<ScmFile> files = getSortedListing(confirmServer);

            assertEquals(3, files.size());
            assertEquals("afolder", files.get(0).getName());
            assertEquals("bfolder", files.get(1).getName());
            assertEquals("foo", files.get(2).getName());

            String foo = IOUtils.inputStreamToString(confirmServer.retrieve(null, "foo", null));
            assertEquals("", foo);
        }
        finally
        {
            IOUtils.close(confirmServer);
        }
    }

    public void testMoveTag() throws ScmException, IOException
    {
        client.tag(null, createRevision(1), TAG_PATH, false);
        client.tag(null, createRevision(8), TAG_PATH, true);
        assertTaggedRev8();
    }

    public void testMoveTagNonExistant() throws ScmException, IOException
    {
        client.tag(null, createRevision(8), TAG_PATH, true);
        assertTaggedRev8();
    }

    private void assertTaggedRev8() throws ScmException, IOException
    {
        SubversionClient confirmServer = null;
        try
        {
            confirmServer = new SubversionClient(TAG_PATH, false, USER, PASSWORD);
            List<ScmFile> files = getSortedListing(confirmServer);

            assertEquals(3, files.size());
            assertEquals("afolder", files.get(0).getName());
            assertEquals("bfolder", files.get(1).getName());
            assertEquals("foo", files.get(2).getName());

            String foo = IOUtils.inputStreamToString(confirmServer.retrieve(null, "foo", null));
            assertEquals("hello\n", foo);
        }
        finally
        {
            IOUtils.close(confirmServer);
        }
    }

    public void testUnmovableTag() throws ScmException
    {
        client.tag(null, createRevision(1), TAG_PATH, false);
        try
        {
            client.tag(null, createRevision(8), TAG_PATH, false);
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Unable to apply tag: path '" + TAG_PATH + "' already exists in the repository", e.getMessage());
        }
    }

    public void testChangesSince() throws ScmException
    {
        List<Changelist> changes = client.getChanges(null, createRevision(2), null);
        assertEquals(2, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("3", changelist.getRevision().getRevisionString());
        assertEquals(1, changelist.getChanges().size());
        assertEquals("/test/trunk/bar", changelist.getChanges().get(0).getPath());
        assertEquals(FileChange.Action.ADD, changelist.getChanges().get(0).getAction());
        changelist = changes.get(1);
        assertEquals("4", changelist.getRevision().getRevisionString());
        assertEquals(1, changelist.getChanges().size());
        assertEquals("/test/trunk/bar", changelist.getChanges().get(0).getPath());
        assertEquals(FileChange.Action.DELETE, changelist.getChanges().get(0).getAction());
    }

    public void testChangesBetweenTwoRevisions() throws ScmException
    {
        List<Changelist> changes = client.getChanges(null, createRevision(2), createRevision(3));
        assertEquals(1, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("3", changelist.getRevision().getRevisionString());
        assertEquals(1, changelist.getChanges().size());
        assertEquals("/test/trunk/bar", changelist.getChanges().get(0).getPath());
        assertEquals(FileChange.Action.ADD, changelist.getChanges().get(0).getAction());
    }

    public void testChangesBetweenReversedRange() throws ScmException
    {
        List<Changelist> changes = client.getChanges(null, createRevision(3), createRevision(2));
        assertEquals(1, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("3", changelist.getRevision().getRevisionString());
        assertEquals(1, changelist.getChanges().size());
        assertEquals("/test/trunk/bar", changelist.getChanges().get(0).getPath());
        assertEquals(FileChange.Action.ADD, changelist.getChanges().get(0).getAction());
    }

    public void testRevisionsSince() throws ScmException
    {
        List<Revision> revisions = client.getRevisions(null, createRevision(2), null);
        assertEquals(2, revisions.size());
        assertEquals("3", revisions.get(0).getRevisionString());
        assertEquals("4", revisions.get(1).getRevisionString());
    }

    public void testRevisionsSinceLatestInFiles() throws ScmException
    {
        List<Revision> revisions = client.getRevisions(null, createRevision(6), null);
        assertEquals(0, revisions.size());
    }

    public void testRevisionsSincePastHead() throws ScmException
    {
        try
        {
            client.getRevisions(null, createRevision(9), null);
        }
        catch (ScmException e)
        {
            assertEquals("svn: No such revision 9", e.getMessage());
        }
    }

    public void testGetRevisionsInRange() throws ScmException
    {
        List<Revision> revisions = client.getRevisions(null, createRevision(2), createRevision(4));
        assertEquals(2, revisions.size());
        assertEquals("3", revisions.get(0).getRevisionString());
        assertEquals("4", revisions.get(1).getRevisionString());
    }

    public void testGetRevisionsInReverseRange() throws ScmException
    {
        List<Revision> revisions = client.getRevisions(null, createRevision(4), createRevision(2));
        assertEquals(2, revisions.size());
        assertEquals("4", revisions.get(0).getRevisionString());
        assertEquals("3", revisions.get(1).getRevisionString());
    }

    public void testCheckout() throws ScmException, IOException
    {
        client.checkout(context, null, null);
        assertRevision(gotDir, 8);
        assertTrue(new File(gotDir, ".svn").isDirectory());
    }

    public void testExport() throws ScmException, IOException
    {
        client.setUseExport(true);
        client.checkout(context, null, null);
        assertRevision(gotDir, 8);
        assertFalse(new File(gotDir, ".svn").isDirectory());
    }
    
    public void testUpdate() throws ScmException, IOException
    {
        client.checkout(context, createRevision(1), null);
        assertRevision(gotDir, 1);
        client.update(context, null, null);
        assertRevision(gotDir, 8);
    }

    public void testMultiUpdate() throws ScmException, IOException
    {
        client.checkout(context, createRevision(1), null);
        client.update(context, createRevision(4), null);
        client.update(context, createRevision(8), null);
        assertRevision(gotDir, 8);
    }

    public void testUpdateDoesNotPrintUnlabelledLines() throws Exception
    {
        RecordingScmFeedbackHandler handler = runRecordedUpdate();
        for (String message: handler.getStatusMessages())
        {
            System.out.println("message = " + message);
        }
    }

    public void testUpdateShowsExpectedStatusLabels() throws Exception
    {
        RecordingScmFeedbackHandler handler = runRecordedUpdate();

        List<String> messages = handler.getStatusMessages();
        assertEquals(1, messages.size());
        String[] pieces = messages.get(0).split(" +");
        assertEquals(2, pieces.length);
        assertEquals("U", pieces[0]);
    }

    private RecordingScmFeedbackHandler runRecordedUpdate() throws ScmException
    {
        client.checkout(context, createRevision(1), null);
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        client.update(context, null, handler);
        return handler;
    }

    public void testUpdateBrokenWorkingCopy() throws Exception
    {
        client.setCleanOnUpdateFailure(true);
        client.checkout(context, createRevision(1), null);
        File rootSvnDir = new File(gotDir, ".svn");
        FileSystemUtils.rmdir(rootSvnDir);
        
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        Revision revision = client.update(context, null, handler);
        assertTrue(rootSvnDir.isDirectory());
        assertEquals(Integer.toString(REVISION_TRUNK_LATEST), revision.getRevisionString());
        assertRevision(gotDir, REVISION_TRUNK_LATEST);
        assertThat(handler.getStatusMessages(), hasItem(containsString("Attempting clean checkout")));
    }

    public void testUpdateBrokenWorkingCopyNoRecovery() throws Exception
    {
        client.setCleanOnUpdateFailure(false);
        client.checkout(context, createRevision(1), null);
        File rootSvnDir = new File(gotDir, ".svn");
        FileSystemUtils.rmdir(rootSvnDir);
        
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        try
        {
            client.update(context, null, handler);
            fail("Expected update to fail");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("not a working copy"));
        }
    }

    public void testCheckNonExistantPathHTTP() throws Exception
    {
        SubversionClient server = null;
        try
        {
            server = new SubversionClient("https://svn.apache.org/repos/asf", false, "anonymous", "");
            assertFalse(server.pathExists(SVNURL.parseURIEncoded("https://svn.apache.org/repos/asf/nosuchpath/")));
        }
        finally
        {
            IOUtils.close(server);
        }
    }

    // CIB-2322: Svn: Unexpected scm trigger when using filters.
    public void testChangesOutsideBasePathIgnored() throws ScmException, IOException, SVNException
    {
        // Drop into a subdirectory to isolate the two changes.
        // We use a file:// url so that the url path does not match the
        // repository path (to catch incorrect use of the url path to filter).
        client = createSubversionClient("file://" + tmpDir.getAbsolutePath() + "/repo/test/trunk/afolder");
        List<Changelist> changelists = client.getChanges(null, createRevision(0), createRevision(1));

        assertEquals(1, changelists.size());
        Changelist changelist = changelists.get(0);
        assertEquals(8, changelist.getChanges().size());

        // exclude the new changes
        client.setFilterPaths(Collections.<String>emptyList(), Arrays.asList("**/afolder/**"));
        changelists = client.getChanges(null, createRevision(0), createRevision(1));

        assertEquals(0, changelists.size());
    }

    private SubversionClient createSubversionClient(String path) throws ScmException
    {
        if (client != null)
        {
            IOUtils.close(client);
        }

        client = new SubversionClient(path, false, USER, PASSWORD);
        return client;
    }

    private void assertRevision(File dir, int revision) throws IOException
    {
        File expectedTestDir = new File(expectedDir, "test");
        if (expectedTestDir.isDirectory())
        {
            removeDirectory(expectedTestDir);
        }
        unzipInput(Integer.toString(revision), expectedDir);
        IOAssertions.assertDirectoriesEqual(new File(expectedTestDir, "trunk"), dir);
    }

    private List<ScmFile> getSortedListing(SubversionClient confirmServer)
            throws ScmException
    {
        List<ScmFile> files = confirmServer.browse(null, "", null);
        Collections.sort(files, new Comparator<ScmFile>()
        {
            public int compare(ScmFile o1, ScmFile o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return files;
    }

    private static Revision createRevision(long rev)
    {
        return new Revision(Long.toString(rev));
    }
}
