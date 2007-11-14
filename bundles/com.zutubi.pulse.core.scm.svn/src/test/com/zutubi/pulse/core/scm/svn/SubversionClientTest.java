package com.zutubi.pulse.core.scm.svn;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmFile;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.IOUtils;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 *
 */
public class SubversionClientTest extends PulseTestCase
{
    private SubversionClient server;
    private File tmpDir;
    private File gotDir;
    private File expectedDir;
    private Process serverProcess;
    private static final String TAG_PATH = "svn://localhost/test/tags/test-tag";
    private ScmContext context;

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
        File dataFile = getTestDataFile("bundles/com.zutubi.pulse.core.scm.svn", "data", "zip");
        tmpDir = FileSystemUtils.createTempDir(getClass().getName(), "");
        File repoDir = new File(tmpDir, "repo");
        repoDir.mkdirs();

        expectedDir = new File(repoDir, "expected");
        expectedDir.mkdirs();

        gotDir = new File(repoDir, "got");
        gotDir.mkdirs();

        context = new ScmContext();
        context.setDir(gotDir);

        ZipUtils.extractZip(dataFile, repoDir);
        serverProcess = Runtime.getRuntime().exec("svnserve --foreground -dr " + repoDir.getAbsolutePath());

        waitForServer(3690);

        server = new SubversionClient("svn://localhost/test/trunk", "jsankey", "password");
    }

    protected void tearDown() throws Exception
    {
        ScmClientUtils.close(server);
        context = null;
        server = null;
        serverProcess.destroy();
        serverProcess.waitFor();
        removeDirectory(tmpDir);
        super.tearDown();
    }

    public void testGetLatestRevision() throws ScmException
    {
        assertEquals("8", server.getLatestRevision().getRevisionString());
    }

    public void testList() throws ScmException
    {
        List<ScmFile> files = server.browse("afolder", null);
        assertEquals(2, files.size());
        assertEquals("f1", files.get(0).getName());
        assertEquals("f2", files.get(1).getName());
    }

    public void testListNonExistent() throws ScmException
    {
        try
        {
            server.browse("nosuchfile", null);
            fail();
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    public void testTag() throws ScmException, IOException
    {
        server.tag(createRevision(1), TAG_PATH, false);

        SubversionClient confirmServer = null;

        try
        {
            confirmServer = new SubversionClient(TAG_PATH, "jsankey", "password");
            List<ScmFile> files = getSortedListing(confirmServer);

            assertEquals(3, files.size());
            assertEquals("afolder", files.get(0).getName());
            assertEquals("bfolder", files.get(1).getName());
            assertEquals("foo", files.get(2).getName());

            String foo = IOUtils.inputStreamToString(confirmServer.retrieve("foo", null));
            assertEquals("", foo);
        }
        finally
        {
            ScmClientUtils.close(confirmServer);
        }
    }

    public void testMoveTag() throws ScmException, IOException
    {
        server.tag(createRevision(1), TAG_PATH, false);
        server.tag(createRevision(8), TAG_PATH, true);
        assertTaggedRev8();
    }

    public void testMoveTagNonExistant() throws ScmException, IOException
    {
        server.tag(createRevision(8), TAG_PATH, true);
        assertTaggedRev8();
    }

    private void assertTaggedRev8() throws ScmException, IOException
    {
        SubversionClient confirmServer = null;
        try
        {
            confirmServer = new SubversionClient(TAG_PATH, "jsankey", "password");
            List<ScmFile> files = getSortedListing(confirmServer);

            assertEquals(3, files.size());
            assertEquals("afolder", files.get(0).getName());
            assertEquals("bfolder", files.get(1).getName());
            assertEquals("foo", files.get(2).getName());

            String foo = IOUtils.inputStreamToString(confirmServer.retrieve("foo", null));
            assertEquals("hello\n", foo);
        }
        finally
        {
            ScmClientUtils.close(confirmServer);
        }
    }

    public void testUnmovableTag() throws ScmException
    {
        server.tag(createRevision(1), TAG_PATH, false);
        try
        {
            server.tag(createRevision(8), TAG_PATH, false);
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Unable to apply tag: path '" + TAG_PATH + "' already exists in the repository", e.getMessage());
        }
    }

    public void testChangesSince() throws ScmException
    {
        List<Changelist> changes = server.getChanges(createRevision(2), null);
        assertEquals(2, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("3", changelist.getRevision().getRevisionString());
        assertEquals(1, changelist.getChanges().size());
        assertEquals("/test/trunk/bar", changelist.getChanges().get(0).getFilename());
        assertEquals(Change.Action.ADD, changelist.getChanges().get(0).getAction());
        changelist = changes.get(1);
        assertEquals("4", changelist.getRevision().getRevisionString());
        assertEquals(1, changelist.getChanges().size());
        assertEquals("/test/trunk/bar", changelist.getChanges().get(0).getFilename());
        assertEquals(Change.Action.DELETE, changelist.getChanges().get(0).getAction());
    }

    public void testRevisionsSince() throws ScmException
    {
        List<Revision> revisions = server.getRevisions(createRevision(2), null);
        assertEquals(2, revisions.size());
        assertEquals("3", revisions.get(0).getRevisionString());
        assertEquals("4", revisions.get(1).getRevisionString());
    }

    public void testRevisionsSinceLatestInFiles() throws ScmException
    {
        List<Revision> revisions = server.getRevisions(createRevision(6), null);
        assertEquals(0, revisions.size());
    }

    public void testRevisionsSincePastHead() throws ScmException
    {
        List<Revision> revisions = server.getRevisions(createRevision(9), null);
        assertEquals(0, revisions.size());
    }

    public void testCheckout() throws ScmException, IOException
    {
        server.checkout(context, null);
        assertRevision(gotDir, 1);
    }

    public void testUpdate() throws ScmException, IOException
    {
        server.checkout(context, null);
        server.update(context, null);
        assertRevision(gotDir, 4);
    }

    public void testMultiUpdate() throws ScmException, IOException
    {
        server.checkout(context, null);
        server.update(context, null);
        server.update(context, null);
        assertRevision(gotDir, 8);
    }

    public void testCheckNonExistantPathHTTP() throws Exception
    {
        SubversionClient server = null;
        try
        {
            server = new SubversionClient("https://svn.apache.org/repos/asf", "anonymous", "");
            assertFalse(server.pathExists(createRevision(1), SVNURL.parseURIEncoded("https://svn.apache.org/repos/asf/nosuchpath/")));
        }
        finally
        {
            ScmClientUtils.close(server);
        }
    }

    private void assertRevision(File dir, int revision) throws IOException
    {
        File dataFile = getTestDataFile("server-core", Integer.toString(revision), "zip");
        ZipUtils.extractZip(dataFile, expectedDir);
        assertDirectoriesEqual(new File(new File(expectedDir, "test"), "trunk"), dir);
    }

    private List<ScmFile> getSortedListing(SubversionClient confirmServer)
            throws ScmException
    {
        List<ScmFile> files = confirmServer.browse("", null);
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
        return new Revision(null, null, null, Long.toString(rev));
    }
}
