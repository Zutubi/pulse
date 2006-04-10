package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.BobTestCase;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * 
 *
 */
public class CvsClientTest extends BobTestCase
{
    //NOTE: when using the 'local' method, ensure that the 'cvs' command

    private CvsClient cvs;

    private File workdir = null;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final SimpleDateFormat CVSDATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    static {
        CVSDATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void setUp() throws Exception
    {
        super.setUp();

        if (com.zutubi.pulse.util.logging.Logger.getLogger(CvsClientTest.class).isLoggable(Level.FINEST))
        {
            Logger.setLogging("system");
        }

        File repositoryRoot = new File(getBobRoot(), "server-core/src/test/com/zutubi/pulse/scm/cvs/repository");
        CVSRoot cvsRoot = CVSRoot.parse(":local:" + repositoryRoot.getCanonicalPath());
        cvs = new CvsClient(cvsRoot);

        // cleanup the working directory.
        workdir = FileSystemUtils.createTempDirectory(CvsClientTest.class.getName(), "");
    }

    public void tearDown() throws Exception
    {
        Logger.setLogging(null);

        removeDirectory(workdir);

        super.tearDown();
    }

//      is located in your path.

    public void testUpdate() throws Exception
    {
        // check that the contents of a particular file are as 
        // expected after the update.
    }

    public void testChangeDetails() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangeDetails";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");

        Change change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsClientTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision());

        changelist = changes.get(1);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt modified by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsClientTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.EDIT, change.getAction());
        assertEquals("1.2", change.getRevision());

        changelist = changes.get(2);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt deleted by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt deleted by author a\n");

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsClientTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.DELETE, change.getAction());
        assertEquals("1.3", change.getRevision());

        changelist = changes.get(3);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file2.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file2.txt checked in by author a\n");

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsClientTest/testChangeDetails/file2.txt", change.getFilename());
        assertEquals(Change.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision());
    }

    public void testChangesByDifferentAuthors() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesByDifferentAuthors";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertEquals(1, changelist.getChanges().size());
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "jason", "file2.txt checked in by author b\n");
        assertEquals(1, changelist.getChanges().size());
        assertChangeValues(changelist.getChanges().get(0), "file2.txt", Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "jason", "", "file2.txt checked in by author b\n");
    }

    public void testChangesByDifferentAuthorsSameFile() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesByDifferentAuthorsSameFile";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "jason", "file1.txt modified by author b\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "jason", "", "file1.txt modified by author b\n");
    }

    public void testChangesBySameAuthorOverlappingFiles() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesBySameAuthorOverlappingFiles";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and file3.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(2), "file3.txt", Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt and file2.txt and file3.txt checked in by author a\n");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file2.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file2.txt", Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file2.txt modified by author a\n");

        changelist = changes.get(3);
        assertChangelistValues(changelist, "daniel", "file1.txt and file 2.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.3");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", Change.Action.EDIT, "1.3");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt and file 2.txt modified by author a\n");
    }

    public void testChangesByOverlappingCommits() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesByOverlappingCommits";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and file3.txt and file4.txt are checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(2), "file3.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(3), "file4.txt", Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt and file2.txt and file3.txt and file4.txt are checked in by author a\n");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "x\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.2");
        assertChangeValues(changelist.getChanges().get(1), "file3.txt", Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "x\n");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "jason", "y\n");
        assertChangeValues(changelist.getChanges().get(0), "file2.txt", Change.Action.EDIT, "1.2");
        assertChangeValues(changelist.getChanges().get(1), "file4.txt", Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "jason", "", "y\n");
    }

    public void testChangesWithRemoval() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesWithRemoval";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt removed by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.DELETE, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt removed by author a\n");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt re-checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.3");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt re-checked in by author a\n");

        changelist = changes.get(3);
        assertChangelistValues(changelist, "daniel", "file1.txt re-removed by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.DELETE, "1.4");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt re-removed by author a\n");
    }

    public void testChangesWithAdd() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesWithAdd";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(1, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and dir/file3.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(2), "dir/file3.txt", Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt and file2.txt and dir/file3.txt checked in by author a\n");
    }

    public void testChangesWithModify() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesWithModify";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.3");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");
    }

    public void testChangesWithBranch() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesWithBranch";
        List<Changelist> changes = cvs.getChangeLists(module, "BRANCH", null, null);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file3.txt checked in on BRANCH by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file3.txt", Change.Action.ADD, "1.1.2.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "BRANCH", "file3.txt checked in on BRANCH by author a\n");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified on BRANCH by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.1.2.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "BRANCH", "file1.txt modified on BRANCH by author a\n");
    }

    public void testCheckoutBranch() throws SCMException, IOException
    {
        String module = "unit-test/CvsClientTest/testCheckoutBranch";
        cvs.setLocalPath(workdir);
        cvs.checkout(module, "BRANCH", null);

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
        assertTrue(new File(workdir, module + "/file3.txt").exists());

        // now checkout head and ensure that file3 is not there.
        removeDirectory(workdir);
        workdir = FileSystemUtils.createTempDirectory(CvsClientTest.class.getName(), "");

        cvs.setLocalPath(workdir);
        cvs.checkout(module);

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertTrue(new File(workdir, module + "/file2.txt").exists());
        assertFalse(new File(workdir, module + "/file3.txt").exists());
    }

    public void testCheckoutDirectory() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testCheckoutDirectory";
        cvs.setLocalPath(workdir);
        cvs.checkout(module + "/dir");

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/dir/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
    }

    public void testCheckoutFile() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testCheckoutFile";
        cvs.setLocalPath(workdir);
        cvs.checkout(module + "/file1.txt");

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
    }

    public void testCheckoutModule() throws SCMException
    {
        String module = "module";
        cvs.setLocalPath(workdir);
        cvs.checkout(module);

        assertTrue(new File(workdir, "unit-test/CvsClientTest/testCheckoutModule/dir1/file1.txt").exists());
        assertTrue(new File(workdir, "unit-test/CvsClientTest/testCheckoutModule/dir2/file2.txt").exists());
    }

    public void testCheckoutByDate() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testCheckoutByDate";
        cvs.setLocalPath(workdir);
        cvs.checkout(module, null, DATE_FORMAT.parse("2006-03-11"));

        // check that the selected files exist.
        assertFalse(new File(workdir, module + "/file1.txt").exists());

        cvs.checkout(module, null, DATE_FORMAT.parse("2006-03-12"));

        assertTrue(new File(workdir, module + "/file1.txt").exists());
    }

    public void testHasBranchChangedSince() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testHasBranchChangedSince";
        assertTrue(cvs.hasChangedSince(module, "BRANCH", DATE_FORMAT.parse("2006-03-10")));
        assertFalse(cvs.hasChangedSince(module, "BRANCH", DATE_FORMAT.parse("2006-03-11")));
    }

    public void testHasDirectoryChangedSince() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testHasDirectoryChangedSince";
        assertTrue(cvs.hasChangedSince(module + "/dir", null, DATE_FORMAT.parse("2006-03-10")));
        assertFalse(cvs.hasChangedSince(module + "/dir", null, DATE_FORMAT.parse("2006-03-11")));
    }

    public void testHasFileChangedSince() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testHasFileChangedSince";
        assertTrue(cvs.hasChangedSince(module + "/file1.txt", null, DATE_FORMAT.parse("2006-03-10")));
        assertFalse(cvs.hasChangedSince(module + "/file1.txt", null, DATE_FORMAT.parse("2006-03-11")));
    }

    public void testHasModuleChangedSince() throws SCMException, ParseException
    {
        String module = "module2";
        assertTrue(cvs.hasChangedSince(module, null, CVSDATE.parse("2006-03-10 14:00:00 GMT")));
        assertFalse(cvs.hasChangedSince(module, null, CVSDATE.parse("2006-03-10 16:00:00 GMT")));
    }

    public void testFileLastChangedDate() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testFileLastChangedDate";
        assertNull(cvs.getLastUpdate(module + "/file1.txt", null, DATE_FORMAT.parse("2006-03-12")));
        Date date = cvs.getLastUpdate(module + "/file1.txt", null, DATE_FORMAT.parse("2006-03-11"));
        assertEquals("2006-03-11 02:00:00 GMT", CVSDATE.format(date));
    }

    public void testDirectoryLastChangedDate() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testDirectoryLastChangedDate";
        assertNull(cvs.getLastUpdate(module, null, DATE_FORMAT.parse("2006-03-12")));
        Date date = cvs.getLastUpdate(module, null, DATE_FORMAT.parse("2006-03-11"));
        assertEquals("2006-03-11 02:30:04 GMT", CVSDATE.format(date));
    }

    public void testRlog() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testRlog";
        List results = cvs.rlog(module, null, null, null, true);
        assertEquals(4, results.size());
    }

    public void testRLogOnBranch() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testRlogOnBranch";
        List results = cvs.rlog(module, "BRANCH", null, null, true);
        assertEquals(4, results.size());
    }

    public void testTestConnection()
    {
        // tset a valid module.
        try
        {
            cvs.testConnection();
        }
        catch (SCMException e)
        {
            fail("did not expect an exception.");
        }
    }

    public void testTestConnectionWithInvalidRoot()
    {
        CVSRoot cvsRoot = CVSRoot.parse(":local:/some/invalid/path");
        try
        {
            new CvsClient(cvsRoot).testConnection();
            fail("expected an exception.");
        }
        catch (SCMException e)
        {
            // noop, exception was expected.
        }
    }

    public void testCheckoutWithoutRecursion() throws SCMException
    {
        cvs.setLocalPath(workdir);
        cvs.checkout("unit-test", null, null, false);
        assertTrue(new File(workdir, "unit-test").exists());
        assertFalse(new File(workdir, "unit-test/CvsServerTest").exists());
    }

    public void testServerVersion() throws SCMException
    {
        String serverVersion = cvs.getServerVersion();
        assertNotNull(serverVersion);
        assertTrue(serverVersion + " does not contain 'CVS'.", serverVersion.contains("CVS"));
    }

    private static void assertChangelistValues(Changelist changelist, String user, String comment)
    {
        assertEquals(user, changelist.getUser());
        assertEquals(comment, changelist.getComment());
    }

    private static void assertChangeValues(Change change, String file, Change.Action action, String revision)
    {
        assertEndsWith(file, change.getFilename());
        assertEquals(action, change.getAction());
        assertEquals(revision, change.getRevision());
    }

// assert that files are unique.

    private static void assertValidChangeSets(List<Changelist> changelists)
    {
        for (Changelist changelist : changelists)
        {
            assertValidChangeSet(changelist);
        }
    }

    private static void assertValidChangeSet(Changelist changelist)
    {
        List<Change> changes = changelist.getChanges();
        Map<String, String> filenames = new HashMap<String, String>();

        for (Change change : changes)
        {
            assertFalse(filenames.containsKey(change.getFilename()));
            filenames.put(change.getFilename(), change.getFilename());

            assertNotNull(change.getRevision());
            assertNotNull(change.getAction());
        }
    }

    private static void assertCvsRevision(Revision revision, String author, String branch, String comment)
    {
        CvsRevision cvsRev = (CvsRevision) revision;
        assertEquals(author, cvsRev.getAuthor());
        assertEquals(branch, cvsRev.getBranch());
        assertEquals(comment, cvsRev.getComment());
    }
}
