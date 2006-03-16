/**
 * <class-comment/>
 */
package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.core.model.CvsRevision;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.test.BobTestCase;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class CvsServerTest extends BobTestCase
{
    private String cvsRoot = null;
    private File workdir = null;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public CvsServerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        Logger.setLogging("system");

        // test repository root.
        File repositoryRoot = new File(getBobRoot(), "server-core/src/test/com/cinnamonbob/scm/cvs/repository");
        cvsRoot = ":local:" + repositoryRoot.getCanonicalPath();

        // cleanup the working directory.
        workdir = FileSystemUtils.createTempDirectory("CvsServer", "Test");
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        removeDirectory(workdir);

        cvsRoot = null;

        super.tearDown();
    }

    private static final SimpleDateFormat CVSDATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void testCheckoutRevisionOfFile() throws SCMException, ParseException
    {
        String module = "unit-test/CvsServerTest/testCheckoutRevisionOfFile";
        CvsServer cvsServer = new CvsServer(cvsRoot, module, null);

        CvsRevision byHead = CvsRevision.HEAD;
        assertEquals("file1.txt latests contents", cvsServer.checkout(byHead, module + "/file1.txt").trim());

        CvsRevision byRevision = new CvsRevision(null, "1.2", null, null);
        assertEquals("file1.txt revision 1.2 contents", cvsServer.checkout(byRevision, module + "/file1.txt").trim());

        byRevision = new CvsRevision(null, "1.1", null, null);
        assertEquals("file1.txt revision 1.1 contents", cvsServer.checkout(byRevision, module + "/file1.txt").trim());

        // checkout the revision of the file based on date.
        Revision byDate = new CvsRevision(null, null, null, CVSDATE.parse("2006-03-11 03:10:07"));
        assertEquals("file1.txt revision 1.2 contents", cvsServer.checkout(byDate, module + "/file1.txt").trim());
    }

    public void testCheckout() throws SCMException, IOException, ParseException
    {
        String module = "unit-test/CvsServerTest/testCheckout";
        CvsServer cvsServer = new CvsServer(cvsRoot, module, null);

        Date before = new Date();
        CvsRevision checkedOutRevision = (CvsRevision) cvsServer.checkout(workdir, CvsRevision.HEAD);
        Date after = new Date();
        assertNotNull(checkedOutRevision.getDate());
        assertTrue(before.compareTo(checkedOutRevision.getDate()) <= 0);
        assertTrue(checkedOutRevision.getDate().compareTo(after) <= 0);

        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertTrue(new File(workdir, module + "/file2.txt").exists());
        assertTrue(new File(workdir, module + "/dir1/file3.txt").exists());
        assertFalse(new File(workdir, module + "/dir2").exists()); // empty directories are pruned.

        FileSystemUtils.cleanOutputDir(workdir);

        // test checkout based on the head revision, requesting the changes betweeen the checkout and the
        LinkedList<Change> changes = null;
        checkedOutRevision = (CvsRevision) cvsServer.checkout(0, workdir, CvsRevision.HEAD, changes);
        assertNotNull(checkedOutRevision);

        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertTrue(new File(workdir, module + "/file2.txt").exists());
        assertTrue(new File(workdir, module + "/dir1/file3.txt").exists());
        assertFalse(new File(workdir, module + "/dir2").exists()); // empty directories are pruned.

        FileSystemUtils.cleanOutputDir(workdir);

        // test checkout based on date before the files were added to the repository.
        Revision byDate = new CvsRevision(null, null, null, CVSDATE.parse("2006-03-11 02:30:00"));
        checkedOutRevision = (CvsRevision) cvsServer.checkout(0, workdir, byDate, changes);
        assertNotNull(checkedOutRevision);

        assertFalse(new File(workdir, module + "/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
        assertFalse(new File(workdir, module + "/dir1/file3.txt").exists());
    }

/*
    public void testCheckout() throws Exception
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "project/test/foobar");
        cvsServer.checkout(workdir, CvsRevision.HEAD);

        // check that the required files exist.
        assertTrue(new File(workdir, "project/test/foobar/bar2").exists());
        assertTrue(new File(workdir, "project/test/foobar/foo2").exists());
        assertFalse(new File(workdir, "project/test/test1").exists());
        assertFalse(new File(workdir, "project/test/test2").exists());
    }

    public void testCheckoutBranch() throws Exception
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "project/test");
        cvsServer.checkout(workdir, new CvsRevision(null, "BRANCH", null, null));

        // check that the required files exist.
        assertTrue(new File(workdir, "project/test/foobar/bar2").exists());
        assertTrue(new File(workdir, "project/test/branch.only").exists());
    }

    public void testCheckoutByDate() throws Exception
    {

        CvsServer cvsServer = new CvsServer(cvsRoot, "project/test");
        cvsServer.checkout(workdir, new CvsRevision(null, null, null, dateFormat.parse("2005-05-08")));

        assertTrue(new File(workdir, "project/test/foobar/bar2").exists());
        assertTrue(new File(workdir, "project/test/foobar/foo2").exists());
        assertTrue(new File(workdir, "project/test/foo").exists());
        assertTrue(new File(workdir, "project/test/bar").exists());
        assertFalse(new File(workdir, "project/test/test1").exists());
        assertFalse(new File(workdir, "project/test/test2").exists());
    }

    public void testGetChanges() throws Exception
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "project/test");
        CvsRevision from = new CvsRevision(null, null, null, dateFormat.parse("2005-05-01"));
        CvsRevision to = new CvsRevision(null, null, null, dateFormat.parse("2005-05-10"));

        assertEquals(5, cvsServer.getChanges(from, to).size());
    }

    public void testGetChangesOnBranch() throws Exception
    {

    }
*/
}