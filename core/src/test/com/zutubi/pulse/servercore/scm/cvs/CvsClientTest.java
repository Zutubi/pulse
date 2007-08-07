package com.zutubi.pulse.servercore.scm.cvs;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.scm.ScmFile;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class CvsClientTest extends PulseTestCase
{
    private String cvsRoot = null;
    private File workdir = null;

    private static final SimpleDateFormat SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public CvsClientTest(String testName)
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

    public void testListRoot() throws ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        List<ScmFile> files = cvsClient.getListing("");
        assertEquals(1, files.size());
        assertEquals("unit-test", files.get(0).getPath());
        assertTrue(files.get(0).isDirectory());
    }

    public void testListing() throws ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        List<ScmFile> files = cvsClient.getListing("unit-test/CvsWorkerTest/testRlog");
        assertEquals(4, files.size());

        String [] expectedNames = new String[]{"file1.txt", "Attic", "dir1", "dir2"};
        Boolean [] expectedTypes = new Boolean[]{false, true, true, true};
        for (int i = 0; i < expectedNames.length; i++)
        {
            assertEquals(expectedNames[i], files.get(i).getName());
            assertEquals(expectedTypes[i], Boolean.valueOf(files.get(i).isDirectory()));
        }
    }

    /*
     * Retrieve the changes between two revisions.
     */
    public void testGetChangesBetween() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenSingleRevision", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:07:00 GMT"));
        CvsRevision to = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:08:00 GMT"));
        List<Changelist> changes = cvsClient.getChanges(from, to);
        assertNotNull(changes);
        assertEquals(1, changes.size());
    }

    /*
     * When requesting the changes between a revision and itself, nothing
     * should be returned.
     */
    public void testGetChangesBetweenSingleRevision() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenSingleRevision", null, null);
        CvsRevision from = new CvsRevision("daniel", "", "", SERVER_DATE.parse("2006-05-08 11:07:15 GMT"));
        List<Changelist> changes = cvsClient.getChanges(from, from);
        assertNotNull(changes);
        assertEquals(0, changes.size());
    }

    /*
     * Verify that the upper bound is included, and the lower bound is not.
     */
    public void testGetChangesBetweenTwoRevisions() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenTwoRevisions", null, null);
        CvsRevision from = new CvsRevision("daniel", "", "", SERVER_DATE.parse("2006-05-08 11:12:24 GMT"));
        CvsRevision to = new CvsRevision("daniel", "", "", SERVER_DATE.parse("2006-05-08 11:16:16 GMT"));
        List<Changelist> changes = cvsClient.getChanges(from, to);
        assertNotNull(changes);
        assertEquals(1, changes.size());
    }

    public void testGetChangesCorrectlyFiltersResults() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsServerTest/testGetChangesCorrectlyFiltersResults", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-06-19 00:00:00 GMT"));
        CvsRevision to = new CvsRevision("", "", "", SERVER_DATE.parse("2006-06-21 00:00:00 GMT"));

        // filter nothing.
        List<Changelist> changes = cvsClient.getChanges(from, to);
        assertEquals(6, changes.get(0).getChanges().size());

        // filter all .txt files
        cvsClient.setExcludedPaths(Arrays.asList("**/*.txt"));
        changes = cvsClient.getChanges(from, to);
        assertEquals(4, changes.get(0).getChanges().size());

        // filter the file.txt files in the subdirectory.
        cvsClient.setExcludedPaths(Arrays.asList("**/directory/file.txt"));
        changes = cvsClient.getChanges(from, to);
        assertEquals(5, changes.get(0).getChanges().size());

        // filter .txt and everything from the directory subdirectory.
        cvsClient.setExcludedPaths(Arrays.asList("**/*.txt", "**/directory/*"));
        changes = cvsClient.getChanges(from, to);
        assertEquals(2, changes.get(0).getChanges().size());

        // filter everything.
        cvsClient.setExcludedPaths(Arrays.asList("**/*"));
        changes = cvsClient.getChanges(from, to);
        assertEquals(0, changes.size());
        assertFalse(cvsClient.hasChangedSince(from));
    }

    public void testListingNonExistent()
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        try
        {
            cvsClient.getListing("nosuchpath");
            fail();
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().contains("does not exist"));
        }

    }

    public void testTestConnection()
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        try
        {
            cvsClient.testConnection();
        }
        catch (ScmException e)
        {
            fail("Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void testTestConnectionInvalidModule()
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "some invalid module here", null, null);
        try
        {
            cvsClient.testConnection();
            fail();
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().contains("module"));
        }
    }

    public void testGetRevisionsSince() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenSingleRevision", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:07:00 GMT"));
        List<Revision> revisions = cvsClient.getRevisionsSince(from);
        assertNotNull(revisions);
        assertEquals(1, revisions.size());
    }

    public void testGetRevision() throws ScmException, ParseException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsClient.getRevision("author:BRANCH:20070201-01:02:33");
        assertEquals("author", revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
    }

    public void testGetRevisionNoAuthor() throws ScmException, ParseException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsClient.getRevision(":BRANCH:20070201-01:02:33");
        assertNull(revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
    }

    public void testGetRevisionNoBranch() throws ScmException, ParseException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsClient.getRevision("author::20070201-01:02:33");
        assertEquals("author", revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
    }

    public void testGetRevisionDateOnly() throws ScmException, ParseException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsClient.getRevision("20070201-01:02:33");
        assertNull(revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
    }

    public void testGetRevisionDayOnly() throws ScmException, ParseException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        CvsRevision revision = cvsClient.getRevision("20070201");
        assertNull(revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-00:00:00"), revision.getDate());
    }

    public void testGetRevisionTooManyPieces() throws ScmException, ParseException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        try
        {
            cvsClient.getRevision("1:2:3:4:5:6:7");
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Invalid CVS revision '1:2:3:4:5:6:7' (must be a date, or <author>:<branch>:<date>)", e.getMessage());
        }
    }

    public void testGetRevisionInvalidDate() throws ScmException, ParseException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test", null, null);
        try
        {
            cvsClient.getRevision("baddate");
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Invalid CVS revision 'baddate': date is invalid: Unparseable date: \"baddate\"", e.getMessage());
        }
    }

}