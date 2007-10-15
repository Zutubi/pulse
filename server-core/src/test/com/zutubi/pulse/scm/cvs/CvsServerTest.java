package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

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
        cvsRoot = ":ext:cvstester:cvs@www.cinnamonbob.com:/cvsroot";

        // cleanup the working directory.
        workdir = FileSystemUtils.createTempDir("CvsServer", "Test");
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
        List<Changelist> changes = cvsServer.getChanges(from, to);
        assertNotNull(changes);
        assertEquals(1, changes.size());
    }

    public void testGetChangesCorrectlyFiltersResults() throws ParseException, SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test/CvsServerTest/testGetChangesCorrectlyFiltersResults", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-06-19 00:00:00 GMT"));
        CvsRevision to = new CvsRevision("", "", "", SERVER_DATE.parse("2006-06-21 00:00:00 GMT"));

        // filter nothing.
        List<Changelist> changes = cvsServer.getChanges(from, to);
        assertEquals(6, changes.get(0).getChanges().size());

        // filter all .txt files
        cvsServer.setExcludedPaths(Arrays.asList("**/*.txt"));
        changes = cvsServer.getChanges(from, to);
        assertEquals(4, changes.get(0).getChanges().size());

        // filter the file.txt files in the subdirectory.
        cvsServer.setExcludedPaths(Arrays.asList("**/directory/file.txt"));
        changes = cvsServer.getChanges(from, to);
        assertEquals(5, changes.get(0).getChanges().size());

        // filter .txt and everything from the directory subdirectory.
        cvsServer.setExcludedPaths(Arrays.asList("**/*.txt", "**/directory/*"));
        changes = cvsServer.getChanges(from, to);
        assertEquals(2, changes.get(0).getChanges().size());

        // filter everything.
        cvsServer.setExcludedPaths(Arrays.asList("**/*"));
        changes = cvsServer.getChanges(from, to);
        assertEquals(0, changes.size());
        assertFalse(cvsServer.hasChangedSince(from));
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
            fail("Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
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

    public void testGetRevisionsSince() throws ParseException, SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenSingleRevision", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:07:00 GMT"));
        CvsRevision to = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:08:00 GMT"));
        List<Revision> revisions = cvsServer.getRevisionsSince(from);
        assertNotNull(revisions);
        assertEquals(1, revisions.size());
    }

    public void testGetRevision() throws SCMException, ParseException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsServer.getRevision("author:BRANCH:20070201-01:02:33");
        assertEquals("author", revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
    }

    public void testGetRevisionNoAuthor() throws SCMException, ParseException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsServer.getRevision(":BRANCH:20070201-01:02:33");
        assertNull(revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
    }

    public void testGetRevisionNoBranch() throws SCMException, ParseException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsServer.getRevision("author::20070201-01:02:33");
        assertEquals("author", revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
    }

    public void testGetRevisionDateOnly() throws SCMException, ParseException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsServer.getRevision("20070201-01:02:33");
        assertNull(revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
    }

    public void testGetRevisionDayOnly() throws SCMException, ParseException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsServer.getRevision("20070201");
        assertNull(revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-00:00:00"), revision.getDate());
    }

    public void testGetRevisionTooManyPieces() throws SCMException, ParseException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        try
        {
            cvsServer.getRevision("1:2:3:4:5:6:7");
            fail();
        }
        catch (SCMException e)
        {
            assertEquals("Invalid CVS revision '1:2:3:4:5:6:7' (must be a date, or <author>:<branch>:<date>)", e.getMessage());
        }
    }

    public void testGetRevisionInvalidDate() throws SCMException, ParseException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        try
        {
            cvsServer.getRevision("baddate");
            fail();
        }
        catch (SCMException e)
        {
            assertEquals("Invalid CVS revision 'baddate' (must be a date, or <author>:<branch>:<date>)", e.getMessage());
        }
    }

    public void testGetRevisionForHead() throws SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsServer.getRevision("::");
        assertNull(revision.getAuthor());
        assertNull(revision.getBranch());
        assertNull(revision.getDate());
    }

    public void testGetRevisionBranchOnly() throws SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsServer.getRevision(":BRANCH:");
        assertNull(revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertNull(revision.getDate());
    }
}