package com.zutubi.pulse.scm.svn;

import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * 
 *
 */
public class SVNServerTest extends PulseTestCase
{
    private SVNServer server;
    private File tmpDir;
    private File repoDir;
    private Process serverProcess;
    private static final String TAG_PATH = "svn://localhost/test/tags/test-tag";

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
        File dataFile = getTestDataFile("server-core", "data", "zip");
        tmpDir = FileSystemUtils.createTempDirectory(getClass().getName(), "");
        repoDir = new File(tmpDir, "repo");
        repoDir.mkdirs();

        FileSystemUtils.extractZip(new ZipInputStream(new FileInputStream(dataFile)), repoDir);
        serverProcess = Runtime.getRuntime().exec("svnserve -d -r " + repoDir.getAbsolutePath());
        server = new SVNServer("svn://localhost/test/trunk", "jsankey", "password");
    }

    protected void tearDown() throws Exception
    {
        server = null;
        serverProcess.destroy();
        removeDirectory(tmpDir);
        super.tearDown();
    }

    public void testGetLatestRevision() throws SCMException
    {
        assertEquals(8L, server.getLatestRevision().getRevisionNumber());
    }

    public void testList() throws SCMException
    {
        List<RemoteFile> files = server.getListing("afolder");
        assertEquals(2, files.size());
        assertEquals("f1", files.get(0).getName());
        assertEquals("f2", files.get(1).getName());
    }

    public void testListNonExistent() throws SCMException
    {
        try
        {
            server.getListing("nosuchfile");
            fail();
        }
        catch (SCMException e)
        {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    public void testTag() throws SCMException
    {
        server.tag(new NumericalRevision(1), TAG_PATH, false);

        SVNServer confirmServer = new SVNServer(TAG_PATH, "jsankey", "password");
        List<RemoteFile> files = getSortedListing(confirmServer);

        assertEquals(3, files.size());
        assertEquals("afolder", files.get(0).getName());
        assertEquals("bfolder", files.get(1).getName());
        assertEquals("foo", files.get(2).getName());

        String foo = confirmServer.checkout(0, null, "foo");
        assertEquals("", foo);
    }

    public void testMoveTag() throws SCMException
    {
        server.tag(new NumericalRevision(1), TAG_PATH, false);
        server.tag(new NumericalRevision(8), TAG_PATH, true);

        SVNServer confirmServer = new SVNServer(TAG_PATH, "jsankey", "password");
        List<RemoteFile> files = getSortedListing(confirmServer);

        assertEquals(3, files.size());
        assertEquals("afolder", files.get(0).getName());
        assertEquals("bfolder", files.get(1).getName());
        assertEquals("foo", files.get(2).getName());

        String foo = confirmServer.checkout(0, null, "foo");
        assertEquals("hello\n", foo);
    }

    public void testUnmovableTag() throws SCMException
    {
        server.tag(new NumericalRevision(1), TAG_PATH, false);
        try
        {
            server.tag(new NumericalRevision(8), TAG_PATH, false);
            fail();
        }
        catch (SCMException e)
        {
            assertEquals("Unable to apply tag: path '" + TAG_PATH + "' already exists in the repository", e.getMessage());
        }
    }

    private List<RemoteFile> getSortedListing(SVNServer confirmServer)
            throws SCMException
    {
        List<RemoteFile> files = confirmServer.getListing("");
        Collections.sort(files, new Comparator<RemoteFile>()
        {
            public int compare(RemoteFile o1, RemoteFile o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return files;
    }
}
