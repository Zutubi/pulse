/**
 * <class-comment/>
 */
package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.model.CvsRevision;
import junit.framework.TestCase;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.text.SimpleDateFormat;

public class CvsServerTest extends TestCase
{
    private String cvsRoot = ":local:/e/cvsroot";

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

        // cleanup the working directory.
        workdir = FileSystemUtils.createTempDirectory("CvsServer", "Test");
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        if (!FileSystemUtils.removeDirectory(workdir))
        {
            throw new RuntimeException("Failed to cleanup test case.");
        }
        super.tearDown();
    }

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
}