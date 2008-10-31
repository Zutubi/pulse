package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
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
        cvsRoot = ":ext:daniel:xxxxx@zutubi.com:/cvsroots/default";
        //":local:" + repoDir.getCanonicalPath()

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

    /*
     * Retrieve the changes between two revisions.
     */
    public void testGetChangesBetween() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenSingleRevision", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:07:00 GMT"));
        CvsRevision to = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:08:00 GMT"));
        List<Changelist> changes = cvsClient.getChanges(null, CvsClient.convertRevision(from), CvsClient.convertRevision(to));
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
        List<Changelist> changes = cvsClient.getChanges(null, CvsClient.convertRevision(from), CvsClient.convertRevision(from));
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
        List<Changelist> changes = cvsClient.getChanges(null, CvsClient.convertRevision(from), CvsClient.convertRevision(to));
        assertNotNull(changes);
        assertEquals(1, changes.size());
    }

    public void testGetChangesCorrectlyFiltersResults() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsServerTest/testGetChangesCorrectlyFiltersResults", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-06-19 00:00:00 GMT"));
        CvsRevision to = new CvsRevision("", "", "", SERVER_DATE.parse("2006-06-21 00:00:00 GMT"));

        // filter nothing.
        List<Changelist> changes = cvsClient.getChanges(null, CvsClient.convertRevision(from), CvsClient.convertRevision(to));
        assertEquals(6, changes.get(0).getChanges().size());

        // filter all .txt files
        cvsClient.setExcludedPaths(Arrays.asList("**/*.txt"));
        changes = cvsClient.getChanges(null, CvsClient.convertRevision(from), CvsClient.convertRevision(to));
        assertEquals(4, changes.get(0).getChanges().size());

        // filter the file.txt files in the subdirectory.
        cvsClient.setExcludedPaths(Arrays.asList("**/directory/file.txt"));
        changes = cvsClient.getChanges(null, CvsClient.convertRevision(from), CvsClient.convertRevision(to));
        assertEquals(5, changes.get(0).getChanges().size());

        // filter .txt and everything from the directory subdirectory.
        cvsClient.setExcludedPaths(Arrays.asList("**/*.txt", "**/directory/*"));
        changes = cvsClient.getChanges(null, CvsClient.convertRevision(from), CvsClient.convertRevision(to));
        assertEquals(2, changes.get(0).getChanges().size());

        // filter everything.
        cvsClient.setExcludedPaths(Arrays.asList("**/*"));
        changes = cvsClient.getChanges(null, CvsClient.convertRevision(from), CvsClient.convertRevision(to));
        assertEquals(0, changes.size());
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

    public void testTestConnectionMultipleModules()
    {
        String modules = "unit-test, integration-test";
        CvsClient cvsClient = new CvsClient(cvsRoot, modules, null, null);
        try
        {
            cvsClient.testConnection();
        }
        catch (ScmException e)
        {
            fail();
        }
    }

    public void testGetRevisionsSince() throws ParseException, ScmException
    {
        CvsClient cvsClient = new CvsClient(cvsRoot, "unit-test/CvsServerTest/testGetChangesBetweenSingleRevision", null, null);
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2006-05-08 11:07:00 GMT"));
        List<Revision> revisions = cvsClient.getRevisions(null, CvsClient.convertRevision(from), null);
        assertNotNull(revisions);
        assertEquals(1, revisions.size());
    }

    public void testGetRevision() throws ScmException, ParseException
    {
        CvsRevision revision = new CvsRevision("author:BRANCH:20070201-01:02:33");
        assertEquals("author", revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertEquals(CvsRevision.DATE_FORMAT.parse("20070201-01:02:33"), revision.getDate());
        assertEquals("author:BRANCH:20070201-01:02:33", revision.getRevisionString());
    }

    public void testMultipleModules() throws ScmException
    {
        String modules = "unit-test, integration-test";
        CvsClient client = new CvsClient(cvsRoot, modules, null, null);

        PulseExecutionContext context = new PulseExecutionContext();
        context.setWorkingDir(workdir);
        context.getScope().setLabel(BuildProperties.SCOPE_RECIPE);
        client.checkout(context, Revision.HEAD, null);

        assertTrue(new File(workdir, "unit-test").isDirectory());
        assertTrue(new File(workdir, "integration-test").isDirectory());
    }
}