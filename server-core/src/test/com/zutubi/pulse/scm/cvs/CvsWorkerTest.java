package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <class-comment/>
 */
public class CvsWorkerTest extends PulseTestCase
{
    /**
     * Local time, formatted based on the EST timezone.
     */
    private static final SimpleDateFormat LOCAL_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static
    {
        LOCAL_DATE.setTimeZone(TimeZone.getTimeZone("EST"));
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Server date, formatted for GMT.
     */
    private static final SimpleDateFormat SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    static
    {
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private File workdir;
    private CvsWorker cvs;
    private CvsRevision fromRevision;
    private CvsRevision toRevision;

    public CvsWorkerTest()
    {
    }

    public CvsWorkerTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        Logger.setLogging("system");

        // test repository root.
        String cvsRoot = ":ext:cvstester:cvs@cinnamonbob.com:/cvsroot";

        cvs = new CvsWorker();
        cvs.setRoot(cvsRoot);

        // two bounding revisions.
        fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));

        // cleanup the working directory.
        workdir = FileSystemUtils.createTempDirectory(CvsWorkerTest.class.getName(), "");
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(workdir);

        cvs = null;

        super.tearDown();
    }

    public void testGetLatestRevision() throws SCMException
    {
        // cvs path for test data.
        String module = "unit-test/CvsWorkerTest";

        cvs.setModule(module);

        Date before = new Date();
        CvsRevision revision = cvs.getLatestChange("test");
//        Date after = new Date();

        assertNotNull(revision);

        // ensure that the date is somewhere in the vacinity of 31 hours old (unless a change
        // has been made to the repository). So, just check that the revision date is no more
        // than 31 hours old.
        assertTrue(before.getTime() - 31 * Constants.HOUR <= revision.getDate().getTime());
//        assertTrue(revision.getDate().getTime() <= after.getTime() - 31 * Constants.HOUR);
    }

    public void testGetLatestRevisionSince() throws SCMException, ParseException
    {
        // cvs path for test data.
        String module = "unit-test/CvsWorkerTest/testGetLatestRevisionSince";
        cvs.setModule(module);

        // The change was made at 2006.03.10.04.00.00 GMT.

        // try a time a couple of days before the change.
        CvsRevision since = new CvsRevision("", "", "", LOCAL_DATE.parse("2006-03-01 02:00:00"));
        CvsRevision revision = cvs.getLatestChange("test", since);
        assertEquals("2006-03-10 04:00:00 GMT", SERVER_DATE.format(revision.getDate()));

        // try a time a couple of days after the change.
        since = new CvsRevision("", "", "", LOCAL_DATE.parse("2006-03-12 02:00:00"));
        revision = cvs.getLatestChange("test", since);
        assertNull(revision);

        // try a time 1 second before the change was made
        since = new CvsRevision("", "", "", SERVER_DATE.parse("2006-03-10 03:59:59 GMT"));
        revision = cvs.getLatestChange("test", since);
        assertEquals("2006-03-10 04:00:00 GMT", SERVER_DATE.format(revision.getDate()));

        // the revision represents the latest change, so if we ask for a change since then,
        // we should get null back.
        assertNull(cvs.getLatestChange("test", revision));
    }

    public void testCheckoutHead() throws SCMException
    {
        String module = "unit-test/CvsWorkerTest/testCheckout";
        cvs.setModule(module);

        cvs.checkout(workdir, CvsRevision.HEAD, null);

        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertTrue(new File(workdir, module + "/file2.txt").exists());
        assertTrue(new File(workdir, module + "/dir1/file3.txt").exists());
        assertTrue(new File(workdir, module + "/dir2").exists()); // empty directories are not pruned.
    }

    public void testCheckoutByDate() throws SCMException, IOException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testCheckout";
        cvs.setModule(module);

        // test checkout based on date before the files were added to the repository.
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-11 02:30:00 GMT"));
        CvsRevision checkedOutRevision = cvs.checkout(workdir, byDate, null);
        assertNotNull(checkedOutRevision);

        assertFalse(new File(workdir, module + "/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
        assertFalse(new File(workdir, module + "/dir1/file3.txt").exists());
    }

    public void testCheckoutFileAtHead() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile";
        cvs.setModule(module);

        CvsRevision byHead = CvsRevision.HEAD;
        String file = module + "/file1.txt";
        cvs.checkoutFile(workdir, byHead, file);

        assertContents("file1.txt latests contents", new File(workdir, file));
    }

    public void testCheckoutFileByRevision() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile";
        cvs.setModule(module);

        CvsRevision byRevision = new CvsRevision(null, "1.2", null, null);
        String file = module + "/file1.txt";
        cvs.checkoutFile(workdir, byRevision, file);
        assertContents("file1.txt revision 1.2 contents", new File(workdir, file));

        byRevision = new CvsRevision(null, "1.1", null, null);
        cvs.checkoutFile(workdir, byRevision, file);
        assertContents("file1.txt revision 1.1 contents", new File(workdir, file));
    }

    public void testCheckoutFileByDate() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile";
        cvs.setModule(module);

        // checkout the revision of the file based on date.
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-11 03:10:07 GMT"));
        String file = module + "/file1.txt";
        cvs.checkoutFile(workdir, byDate, file);
        assertContents("file1.txt revision 1.2 contents", new File(workdir, file));
    }

    public void testCheckoutModule() throws SCMException
    {
        String module = "module";
        cvs.setModule(module);

        cvs.checkout(workdir, CvsRevision.HEAD, null);

        assertTrue(new File(workdir, "unit-test/CvsWorkerTest/testCheckoutModule/dir1/file1.txt").exists());
        assertTrue(new File(workdir, "unit-test/CvsWorkerTest/testCheckoutModule/dir2/file2.txt").exists());
    }

    public void testCheckoutBranch() throws SCMException, IOException
    {
        String module = "unit-test/CvsWorkerTest/testCheckoutBranch";
        cvs.setModule(module);
        cvs.setBranch("BRANCH");
        cvs.checkout(workdir, CvsRevision.HEAD, null);

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
        assertTrue(new File(workdir, module + "/file3.txt").exists());

        // now checkout head and ensure that file3 is not there.
        FileSystemUtils.cleanOutputDir(workdir);

        cvs.setBranch(null);
        cvs.checkout(workdir, CvsRevision.HEAD, null);

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertTrue(new File(workdir, module + "/file2.txt").exists());
        assertFalse(new File(workdir, module + "/file3.txt").exists());
    }

    public void testGetChangesBetween() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        cvs.setModule(module);

        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 01:00:00 GMT"));

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

        assertEquals(2, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt modified by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");

        Change change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.EDIT, change.getAction());
        assertEquals("1.2", change.getRevision());

        changelist = changes.get(1);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt deleted by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt deleted by author a\n");

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.DELETE, change.getAction());
        assertEquals("1.3", change.getRevision());
    }

    public void testChangeDetails() throws SCMException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        cvs.setModule(module);

        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-02-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 01:00:00 GMT"));

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

        assertEquals(4, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");

        Change change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision());

        changelist = changes.get(1);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt modified by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.EDIT, change.getAction());
        assertEquals("1.2", change.getRevision());

        changelist = changes.get(2);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt deleted by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt deleted by author a\n");

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.DELETE, change.getAction());
        assertEquals("1.3", change.getRevision());

        changelist = changes.get(3);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file2.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file2.txt checked in by author a\n");

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/file2.txt", change.getFilename());
        assertEquals(Change.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision());
    }

    public void testChangesByDifferentAuthors() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesByDifferentAuthors";
        cvs.setModule(module);

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

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

    public void testChangesByOverlappingCommits() throws SCMException
    {
        String module = "unit-test/CvsWorkerTest/testChangesByOverlappingCommits";
        cvs.setModule(module);

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

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
        String module = "unit-test/CvsWorkerTest/testChangesWithRemoval";
        cvs.setModule(module);

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

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
        String module = "unit-test/CvsWorkerTest/testChangesWithAdd";
        cvs.setModule(module);

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

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
        String module = "unit-test/CvsWorkerTest/testChangesWithModify";
        cvs.setModule(module);

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

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
        String module = "unit-test/CvsWorkerTest/testChangesWithBranch";
        cvs.setModule(module);
        cvs.setBranch("BRANCH");

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

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

    public void testChangesOnHeadAndBranch() throws SCMException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testChangesOnHeadAndBranch";
        cvs.setModule(module);

        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-10-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-10-18 00:00:00 GMT"));

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);
        assertEquals(2, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("", changelist.getRevision().getBranch());
        changelist = changes.get(1);
        assertEquals("", changelist.getRevision().getBranch());

        // now check that we can do the same on the branch.
        cvs.setBranch("BRANCH");
        fromRevision.setBranch("BRANCH");
        toRevision.setBranch("BRANCH");

        changes = cvs.getChangesBetween("test", fromRevision, toRevision);
        assertEquals(1, changes.size());
        changelist = changes.get(0);
        assertEquals("BRANCH", changelist.getRevision().getBranch());
    }

    public void testHasBranchChangedSince() throws SCMException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testHasBranchChangedSince";
        cvs.setModule(module);
        cvs.setBranch("BRANCH");

        assertNotNull(cvs.getLatestChange("test", new CvsRevision("", "", "", DATE_FORMAT.parse("2006-03-10"))));
        assertNull(cvs.getLatestChange("test", new CvsRevision("", "", "", DATE_FORMAT.parse("2006-03-11"))));
    }

    public void testHasFileChangedSince() throws SCMException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testHasFileChangedSince";
        cvs.setModule(module + "/file1.txt");

        assertNotNull(cvs.getLatestChange("test", new CvsRevision("", "", "", DATE_FORMAT.parse("2006-03-10"))));
        assertNull(cvs.getLatestChange("test", new CvsRevision("", "", "", DATE_FORMAT.parse("2006-03-11"))));
    }

    public void testHasModuleChangedSince() throws SCMException, ParseException
    {
        cvs.setModule("module2");

        assertNotNull(cvs.getLatestChange("test", new CvsRevision("", "", "", SERVER_DATE.parse("2006-03-10 14:00:00 GMT"))));
        assertNull(cvs.getLatestChange("test", new CvsRevision("", "", "", SERVER_DATE.parse("2006-03-10 16:00:00 GMT"))));
    }

    public void testFileLastChangedDate() throws SCMException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testFileLastChangedDate";
        cvs.setModule(module + "/file1.txt");

        assertNull(cvs.getLatestChange("test", new CvsRevision("", "", "", DATE_FORMAT.parse("2006-03-12"))));
        CvsRevision rev = cvs.getLatestChange("test", new CvsRevision("", "", "", DATE_FORMAT.parse("2006-03-11")));
        assertEquals("2006-03-11 02:00:00 GMT", SERVER_DATE.format(rev.getDate()));
    }

    public void testRepositoryListing() throws SCMException
    {
        cvs.setModule("unit-test");

        List<String> dirListing = cvs.getListing();
        assertTrue(dirListing.size() > 40);
    }

    public void testUpdateToHead() throws ParseException, SCMException
    {
        String module = "unit-test/CvsWorkerTest/testUpdateOnHead";
        cvs.setModule(module);

        // checkout is required first.    2006.05.10.13.33.54
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:33:00 GMT"));
        cvs.checkout(workdir, byDate, null);

        // verify that file x is not there.
        File x = new File(workdir, module + "/file1.txt");
        assertFalse(x.exists());

        // cvs update to a specific date.
        cvs.update(workdir, CvsRevision.HEAD, null);

        // verify that file x is now there.
        assertTrue(x.exists());
    }

    public void testUpdateToDate() throws SCMException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testUpdateOnHead";
        cvs.setModule(module);

        // checkout is required first.    2006.05.10.13.33.54
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:33:00 GMT"));
        cvs.checkout(workdir, byDate, null);

        // verify that file x is not there.
        File x = new File(workdir, module + "/file1.txt");
        assertFalse(x.exists());

        // cvs update to a specific date.
        cvs.update(workdir, new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:34:00 GMT")), null);

        // verify that file x is now there.
        assertTrue(x.exists());
    }

    public void testTagContent() throws SCMException, IOException
    {
        String module = "unit-test/CvsWorkerTest/testTagContent";
        cvs.setModule(module);

        File baseCheckoutDir = new File(workdir, module);

        // create a random tag for testing.
        String tagName = "T_" + String.valueOf(System.currentTimeMillis());

        // checkout by tag and verify that no content is there.
        CvsRevision tag = new CvsRevision(null, tagName, null, null);
        try
        {
            cvs.checkout(workdir, tag, null);
            fail();
        }
        catch (SCMException e)
        {
            // expect exception because tag should not exist.
        }

        // clean out the working directory.
        FileSystemUtils.cleanOutputDir(workdir);

        // tag the content.
        cvs.tag(CvsRevision.HEAD, tagName, false);

        // checkout by tag and verify that the expected content is there.
        cvs.checkout(workdir, tag, null);
        assertTrue(new File(baseCheckoutDir, "file.txt").exists());
    }

    public void testDeleteTag() throws SCMException, IOException
    {
        String module = "unit-test/CvsWorkerTest/testTagContent";
        cvs.setModule(module);
        String tagName = "T_" + String.valueOf(System.currentTimeMillis());

        // create tag.
        cvs.tag(CvsRevision.HEAD, tagName, false);

        // checkout to ensure that the tag has been created.
        CvsRevision tag = new CvsRevision(null, tagName, null, null);
        cvs.checkout(workdir, tag, null);

        FileSystemUtils.cleanOutputDir(workdir);

        // delete tag.
        cvs.deleteTag(tag);

        // ensure that tag contains nothing.
        tag = new CvsRevision(null, tagName, null, null);
        cvs.checkout(workdir, tag, null);
        // because we have created the tag in the past, the subsequent checkout will succeed, but should
        // generate an empty checkout directory.
        assertFalse(new File(workdir, module + "/file.txt").exists());
    }

    //---( CIB-831 )---

    public void testAddToBranchDoesNotAppearOnHead() throws SCMException
    {
        // In testChangesWithBranch, file1 and file2 are added to head. We then branch, add file3
        // to the branch and edit file1.
        // a) expect 2 changes on branch - the add and the edit.
        // b) expect 1 change on head - initial add

        String module = "unit-test/CvsWorkerTest/testChangesWithBranch";
        cvs.setModule(module);
        cvs.setBranch("BRANCH");

        List<Changelist> changes = cvs.getChangesBetween("test", fromRevision, toRevision);

        assertEquals(2, changes.size());

        cvs.setBranch(null);

        changes = cvs.getChangesBetween("test", fromRevision, toRevision);
        assertEquals(1, changes.size());
    }

    private void assertContents(String expected, File file) throws IOException
    {
        assertEquals(expected, IOUtils.fileToString(file).trim());
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
