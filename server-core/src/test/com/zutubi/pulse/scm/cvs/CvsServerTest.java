/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
/**
 * <class-comment/>
 */
package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.Changelist;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class CvsServerTest extends PulseTestCase
{
    private String cvsRoot = null;
    private File workdir = null;

    private static final SimpleDateFormat SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public CvsServerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        Logger.setLogging("system");

        // test repository root.
        File repositoryRoot = new File(getPulseRoot(), "server-core/src/test/com/zutubi/pulse/scm/cvs/repository");
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

    public void testListRoot() throws SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        List<RemoteFile> files = cvsServer.getListing("");
        assertEquals(1, files.size());
        assertEquals("unit-test", files.get(0).getPath());
        assertTrue(files.get(0).isDirectory());
    }

    public void testListing() throws SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        List<RemoteFile> files = cvsServer.getListing("unit-test/CvsWorkerTest/testRlog");
        assertEquals(4, files.size());

        String [] expectedNames = new String[]{"file1.txt", "Attic", "dir1", "dir2"};
        Boolean [] expectedTypes = new Boolean[]{false, true, true, true};
        for (int i = 0; i < expectedNames.length; i++)
        {
            assertEquals(expectedNames[i], files.get(i).getName());
            assertEquals(expectedTypes[i], Boolean.valueOf(files.get(i).isDirectory()));
        }
    }

    /**
     * Retrieve the changes between two revisions.
     */
    public void testGetChangesBetween() throws ParseException, SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenSingleRevision", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:07:00 GMT"));
        CvsRevision to = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:08:00 GMT"));
        List<Changelist> changes = cvsServer.getChanges(from, to, "");
        assertNotNull(changes);
        assertEquals(1, changes.size());
    }

    /**
     * When requesting the changes between a revision and itself, nothing
     * should be returned.
     */
    public void testGetChangesBetweenSingleRevision() throws ParseException, SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenSingleRevision", null, null);
        CvsRevision from = new CvsRevision("daniel", "", "", SERVER_DATE.parse("2006-05-08 11:07:15 GMT"));
        List<Changelist> changes = cvsServer.getChanges(from, from, "");
        assertNotNull(changes);
        assertEquals(0, changes.size());
    }

    /**
     * Verify that the upper bound is included, and the lower bound is not.
     */
    public void testGetChangesBetweenTwoRevisions() throws ParseException, SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenTwoRevisions", null, null);
        CvsRevision from = new CvsRevision("daniel", "", "", SERVER_DATE.parse("2006-05-08 11:12:24 GMT"));
        CvsRevision to = new CvsRevision("daniel", "", "", SERVER_DATE.parse("2006-05-08 11:16:16 GMT"));
        List<Changelist> changes = cvsServer.getChanges(from, to, "");
        assertNotNull(changes);
        assertEquals(1, changes.size());
    }

    public void testListingNonExistent()
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
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
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
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
        CvsServer cvsServer = new CvsServer(cvsRoot, "some invalid module here", null, null);
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