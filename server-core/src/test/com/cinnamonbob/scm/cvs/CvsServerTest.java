/**
 * <class-comment/>
 */
package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.CvsRevision;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.filesystem.remote.RemoteFile;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.test.BobTestCase;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CvsServerTest extends BobTestCase
{
    private String cvsRoot = null;
    private File workdir = null;

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
    static
    {
        CVSDATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void testCheckoutFileAtHead() throws SCMException
    {
        String module = "unit-test/CvsServerTest/testCheckoutRevisionOfFile";
        CvsServer cvsServer = new CvsServer(cvsRoot, module, null);

        CvsRevision byHead = CvsRevision.HEAD;
        assertEquals("file1.txt latests contents", cvsServer.checkout(1, byHead, module + "/file1.txt").trim());
    }

    public void testCheckoutFileByRevision() throws SCMException
    {
        String module = "unit-test/CvsServerTest/testCheckoutRevisionOfFile";
        CvsServer cvsServer = new CvsServer(cvsRoot, module, null);

        CvsRevision byRevision = new CvsRevision(null, "1.2", null, null);
        assertEquals("file1.txt revision 1.2 contents", cvsServer.checkout(1, byRevision, module + "/file1.txt").trim());

        byRevision = new CvsRevision(null, "1.1", null, null);
        assertEquals("file1.txt revision 1.1 contents", cvsServer.checkout(1, byRevision, module + "/file1.txt").trim());
    }

    public void testCheckoutFileByDate() throws SCMException, ParseException
    {
        String module = "unit-test/CvsServerTest/testCheckoutRevisionOfFile";
        CvsServer cvsServer = new CvsServer(cvsRoot, module, null);

        // checkout the revision of the file based on date.
        Revision byDate = new CvsRevision(null, null, null, CVSDATE.parse("2006-03-11 03:10:07"));
        assertEquals("file1.txt revision 1.2 contents", cvsServer.checkout(1, byDate, module + "/file1.txt").trim());
    }

    public void testCheckoutHead() throws SCMException, IOException, ParseException
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
        assertTrue(new File(workdir, module + "/dir2").exists()); // empty directories are not pruned.

    }
    public void testCheckoutByDate() throws SCMException, IOException, ParseException
    {
        String module = "unit-test/CvsServerTest/testCheckout";
        CvsServer cvsServer = new CvsServer(cvsRoot, module, null);

        // test checkout based on date before the files were added to the repository.
        Revision byDate = new CvsRevision(null, null, null, CVSDATE.parse("2006-03-11 02:30:00"));
        List<Change> changes = null;
        CvsRevision checkedOutRevision = (CvsRevision) cvsServer.checkout(0, workdir, byDate, changes);
        assertNotNull(checkedOutRevision);

        assertFalse(new File(workdir, module + "/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
        assertFalse(new File(workdir, module + "/dir1/file3.txt").exists());
    }

    public void testListRoot() throws SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null);
        List<RemoteFile> files = cvsServer.getListing("");
        assertEquals(1, files.size());
        assertEquals("unit-test", files.get(0).getPath());
        assertTrue(files.get(0).isDirectory());
    }

    public void testListing() throws SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null);
        List<RemoteFile> files = cvsServer.getListing("unit-test/CvsClientTest/testRlog");
        assertEquals(4, files.size());

        String [] expectedNames = new String[]{"file1.txt", "Attic", "dir1", "dir2"};
        Boolean [] expectedTypes = new Boolean[]{false, true, true, true};
        for (int i = 0; i < expectedNames.length; i++)
        {
            assertEquals(expectedNames[i], files.get(i).getName());
            assertEquals((boolean) expectedTypes[i], files.get(i).isDirectory());
        }
    }

    public void testListingNonExistent()
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null);
        try
        {
            cvsServer.getListing("nosuchpath");
            fail();
        }
        catch (SCMException e)
        {
            assertTrue(e.getMessage().contains("does not exist"));
        }

    }

    public void testTestConnection()
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null);
        try
        {
            cvsServer.testConnection();
        }
        catch (SCMException e)
        {
            fail();
        }
    }

    public void testTestConnectionInvalidModule()
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "some invalid module here", null);
        try
        {
            cvsServer.testConnection();
            fail();
        }
        catch (SCMException e)
        {
            assertTrue(e.getMessage().contains("module"));
        }
    }
}