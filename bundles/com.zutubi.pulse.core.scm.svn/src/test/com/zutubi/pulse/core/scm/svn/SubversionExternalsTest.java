package com.zutubi.pulse.core.scm.svn;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.*;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
public class SubversionExternalsTest extends PulseTestCase
{
    private SubversionClient server;
    private File tempDir;
    private File checkoutDir;
    private Process svnProcess;

    //    jsankey@tiberius-v ~/ext/all
    //    $ svn -v log
    //    ------------------------------------------------------------------------
    //    r8 | (no author) | 2006-11-18 17:07:15 +1100 (Sat, 18 Nov 2006) | 1 line
    //    Changed paths:
    //       M /bundle/trunk/file2
    //       M /ext1/trunk/file2
    //       M /ext2/trunk/file2
    //       M /meta/trunk/file2
    //    
    //    Edit in all
    //    ------------------------------------------------------------------------
    //    r7 | (no author) | 2006-11-18 17:06:20 +1100 (Sat, 18 Nov 2006) | 1 line
    //    Changed paths:
    //       M /meta/trunk/file1
    //
    //    Edit in meta
    //    ------------------------------------------------------------------------
    //    r6 | (no author) | 2006-11-18 17:06:06 +1100 (Sat, 18 Nov 2006) | 1 line
    //    Changed paths:
    //       M /ext1/trunk/file1
    //
    //    Edit in ext1
    //    ------------------------------------------------------------------------
    //    r5 | (no author) | 2006-11-18 17:05:46 +1100 (Sat, 18 Nov 2006) | 1 line
    //    Changed paths:
    //       M /bundle/trunk/file1
    //
    //    Edit in bundle
    //    ------------------------------------------------------------------------
    //    r4 | (no author) | 2006-11-18 17:04:21 +1100 (Sat, 18 Nov 2006) | 1 line
    //    Changed paths:
    //       M /ext2/trunk
    //
    //    Fixed meta external on ext2.
    //    ------------------------------------------------------------------------
    //    r3 | (no author) | 2006-11-18 17:03:24 +1100 (Sat, 18 Nov 2006) | 1 line
    //    Changed paths:
    //       M /ext2/trunk
    //
    //    Added meta external to ext2.
    //    ------------------------------------------------------------------------
    //    r2 | (no author) | 2006-11-18 17:02:19 +1100 (Sat, 18 Nov 2006) | 1 line
    //    Changed paths:
    //       M /bundle/trunk
    //
    //    Added externals to bundle.
    //    ------------------------------------------------------------------------
    //    r1 | (no author) | 2006-11-18 16:57:35 +1100 (Sat, 18 Nov 2006) | 1 line
    //    Changed paths:
    //       A /bundle
    //       A /bundle/trunk
    //       A /bundle/trunk/file1
    //       A /bundle/trunk/file2
    //       A /ext1
    //       A /ext1/trunk
    //       A /ext1/trunk/file1
    //       A /ext1/trunk/file2
    //       A /ext2
    //       A /ext2/trunk
    //       A /ext2/trunk/file1
    //       A /ext2/trunk/file2
    //       A /meta
    //       A /meta/trunk
    //       A /meta/trunk/file1
    //       A /meta/trunk/file2
    //
    //    Initil import
    //    ------------------------------------------------------------------------

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(getName(), "");
        tempDir = tempDir.getCanonicalFile();

        checkoutDir = new File(tempDir, "checkout");
        checkoutDir.mkdir();

        // Create empty repo
        File repoDir = new File(tempDir, "repo");
        repoDir.mkdir();
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnadmin", "create", repoDir.getAbsolutePath() });
        svnProcess.waitFor();

        // Allow anonymous writes
        File conf = new File(repoDir, FileSystemUtils.composeFilename("conf", "svnserve.conf"));
        FileSystemUtils.createFile(conf, "[general]\nanon-access = write\nauth-access = write\n");

        // Restore from dump
        File repoZip = getTestDataFile("bundles/com.zutubi.pulse.core.scm.svn", "repo", "zip");
        ZipUtils.extractZip(repoZip, tempDir);

        File dump = new File(tempDir, "SubversionExternalsTest.repo");
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnadmin", "load", "-q", repoDir.getAbsolutePath() });
        FileInputStream is = new FileInputStream(dump);
        IOUtils.joinStreams(is, svnProcess.getOutputStream());
        svnProcess.getOutputStream().close();
        is.close();
        svnProcess.waitFor();

        // Start svn server
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnserve", "--foreground", "-dr", "."}, null, repoDir);
        waitForServer(3690);

        server = new SubversionClient("svn://localhost/bundle/trunk");
    }

    protected void tearDown() throws Exception
    {
        ScmClientUtils.close(server);
        server = null;
        svnProcess.destroy();
        svnProcess.waitFor();
        Thread.sleep(1000);
        
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testGetExternals() throws Exception
    {
        server.addExternalPath(".");
        List<SubversionClient.ExternalDefinition> externals = server.getExternals(createRevision(8));
        assertEquals(2, externals.size());
        assertExternal(externals.get(0), "pull1", "svn://localhost/ext1/trunk");
        assertExternal(externals.get(1), "pull2", "svn://localhost/ext2/trunk");
    }

    public void testGetExternalsNoPath() throws Exception
    {
        List<SubversionClient.ExternalDefinition> externals = server.getExternals(createRevision(8));
        assertEquals(0, externals.size());
    }

    public void testGetChangesOnExternal() throws Exception
    {
        server.addExternalPath(".");
        List<Changelist> changes = server.getChanges(createRevision(5), createRevision(6));
        assertEquals(1, changes.size());
        assertChange(changes.get(0), "6", "/ext1/trunk/file1");
    }

    public void testGetChangesOnExternalAndBundle() throws Exception
    {
        server.addExternalPath(".");
        List<Changelist> changes = server.getChanges(createRevision(4), createRevision(6));
        assertEquals(2, changes.size());
        assertChange(changes.get(0), "5", "/bundle/trunk/file1");
        assertChange(changes.get(1), "6", "/ext1/trunk/file1");
    }

    public void testGetChangesOnMetaExternals() throws Exception
    {
        server.addExternalPath(".");
        List<Changelist> changes = server.getChanges(createRevision(6), createRevision(7));
        assertEquals(0, changes.size());
    }

    public void testGetChangesOnAll() throws Exception
    {
        server.addExternalPath(".");
        List<Changelist> changes = server.getChanges(createRevision(7), createRevision(8));
        assertEquals(1, changes.size());
        assertChange(changes.get(0), "8", "/bundle/trunk/file2", "/ext1/trunk/file2", "/ext2/trunk/file2", "/meta/trunk/file2");
    }

    public void testCheckoutRevision() throws Exception
    {
        doCheckout(5);

        assertFile("file1", "edited bundle file1\n");
        assertFile("pull1/file1", "");
        assertFile("pull1/file2", "");
        assertFile("pull2/file2", "");
    }

    public void testCheckoutLastRevision() throws Exception
    {
        doCheckout(8);

        assertFile("file2", "edit in all\n");
        assertFile("pull1/file2", "edit in all\n");
        assertFile("pull2/file2", "edit in all\n");
    }

    public void testUpdate() throws Exception
    {
        doCheckout(2);
        ScmContext context = new ScmContext();
        context.setRevision(new Revision("5"));
        context.setDir(checkoutDir);
        server.update(context, null);

        assertFile("file1", "edited bundle file1\n");
        assertFile("pull1/file1", "");
        assertFile("pull1/file2", "");
        assertFile("pull2/file2", "");
    }

    private void doCheckout(int rev) throws ScmException
    {
        ScmContext context = new ScmContext();
        Revision revision = new Revision(Integer.toString(rev));
        context.setRevision(revision);
        context.setDir(checkoutDir);
        server.addExternalPath(".");
        server.checkout(context, new ScmEventHandler()
        {
            public void status(String message)
            {
                System.out.println(message);
            }

            public void fileChanged(Change change)
            {
                System.out.println(change.toString());
            }

            public void checkCancelled() throws ScmCancelledException
            {
            }
        });
    }

    private void assertChange(Changelist changelist, String revision, String... paths)
    {
        assertEquals(revision, changelist.getRevision().getRevisionString());
        List<Change> changes = changelist.getChanges();
        Collections.sort(changes, new Comparator<Change>()
        {
            public int compare(Change o1, Change o2)
            {
                return o1.getFilename().compareTo(o2.getFilename());
            }
        });
        
        for(int i = 0; i < paths.length; i++)
        {
            Change change = changes.get(i);
            assertEquals(paths[i], change.getFilename());
        }
    }

    private void assertExternal(SubversionClient.ExternalDefinition external, String path, String url)
    {
        assertEquals(path, external.path);
        assertEquals(url, external.url.toDecodedString());
    }

    private void assertFile(String path, String content) throws IOException
    {
        File f = new File(checkoutDir, path);
        assertEquals(content, IOUtils.fileToString(f));
    }

    public static void main(String[] argv) throws ScmException
    {
        SubversionClient server = null;
        try
        {
            server = new SubversionClient("http://svn.nuxeo.org/nuxeo/bundles/ECM-trunk");
            server.addExternalPath(".");
            List<Changelist> changelists = server.getChanges(createRevision(6600), createRevision(6603));
            for(Changelist list: changelists)
            {
                System.out.println(list.getRevision().getRevisionString() + ": " + list.getComment());
                for(Change change: list.getChanges())
                {
                    System.out.println("    " + change.toString());
                }
            }
        }
        finally
        {
            ScmClientUtils.close(server);
        }
    }

    private static Revision createRevision(long rev)
    {
        return new Revision(null, null, null, Long.toString(rev));
    }
}
