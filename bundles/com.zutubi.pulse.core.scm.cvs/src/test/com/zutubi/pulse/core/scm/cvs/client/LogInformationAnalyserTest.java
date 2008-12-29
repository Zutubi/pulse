package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.cvs.CvsClient;
import com.zutubi.pulse.core.scm.cvs.CvsRevision;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.core.test.StringAssertions;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.util.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * 
 */
public class LogInformationAnalyserTest extends PulseTestCase
{
    private static final SimpleDateFormat LOCAL_DATE;
    private static final SimpleDateFormat SERVER_DATE;

    static 
    {
        LOCAL_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LOCAL_DATE.setTimeZone(TimeZone.getTimeZone("EST"));
        SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private CvsCore cvs;
    private LogInformationAnalyser analyser;

    public LogInformationAnalyserTest()
    {
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        Logger.setLogging("system");

        String cvsRoot = ":ext:daniel:xxxxx@zutubi.com:/cvsroots/default";
        cvs = new CvsCore();
        cvs.setRoot(CVSRoot.parse(cvsRoot));
        analyser = new LogInformationAnalyser(CVSRoot.parse(cvsRoot));
    }

    protected void tearDown() throws Exception
    {
        cvs = null;
        analyser = null;
    }

    public void testGetChangesBetween() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 01:00:00 GMT"));
        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, null);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = changes.get(0);
        assertEquals("daniel", changelist.getAuthor());
        assertEquals("file1.txt modified by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt modified by author a\n");
        FileChange change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getPath());
        assertEquals(FileChange.Action.EDIT, change.getAction());
        assertEquals("1.2", change.getRevision().getRevisionString());
        changelist = changes.get(1);
        assertEquals("daniel", changelist.getAuthor());
        assertEquals("file1.txt deleted by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt deleted by author a\n");
        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getPath());
        assertEquals(FileChange.Action.DELETE, change.getAction());
        assertEquals("1.3", change.getRevision().getRevisionString());
    }

    public void testChangeDetails() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-02-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 01:00:00 GMT"));
        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, null);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = changes.get(0);
        assertEquals("daniel", changelist.getAuthor());
        assertEquals("file1.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt checked in by author a\n");
        FileChange change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getPath());
        assertEquals(FileChange.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision().getRevisionString());
        changelist = changes.get(1);
        assertEquals("daniel", changelist.getAuthor());
        assertEquals("file1.txt modified by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt modified by author a\n");
        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getPath());
        assertEquals(FileChange.Action.EDIT, change.getAction());
        assertEquals("1.2", change.getRevision().getRevisionString());
        changelist = changes.get(2);
        assertEquals("daniel", changelist.getAuthor());
        assertEquals("file1.txt deleted by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt deleted by author a\n");
        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getPath());
        assertEquals(FileChange.Action.DELETE, change.getAction());
        assertEquals("1.3", change.getRevision().getRevisionString());
        changelist = changes.get(3);
        assertEquals("daniel", changelist.getAuthor());
        assertEquals("file2.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file2.txt checked in by author a\n");
        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/file2.txt", change.getPath());
        assertEquals(FileChange.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision().getRevisionString());
    }

    public void testChangesByDifferentAuthors() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesByDifferentAuthors";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, null);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertEquals(1, changelist.getChanges().size());
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt checked in by author a\n");
        changelist = changes.get(1);
        assertChangelistValues(changelist, "jason", "file2.txt checked in by author b\n");
        assertEquals(1, changelist.getChanges().size());
        assertChangeValues(changelist.getChanges().get(0), "file2.txt", FileChange.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "jason", null, "file2.txt checked in by author b\n");
    }

    public void testChangesByOverlappingCommits() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesByOverlappingCommits";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, null);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and file3.txt and file4.txt are checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", FileChange.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(2), "file3.txt", FileChange.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(3), "file4.txt", FileChange.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt and file2.txt and file3.txt and file4.txt are checked in by author a\n");
        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "x\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.EDIT, "1.2");
        assertChangeValues(changelist.getChanges().get(1), "file3.txt", FileChange.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "x\n");
        changelist = changes.get(2);
        assertChangelistValues(changelist, "jason", "y\n");
        assertChangeValues(changelist.getChanges().get(0), "file2.txt", FileChange.Action.EDIT, "1.2");
        assertChangeValues(changelist.getChanges().get(1), "file4.txt", FileChange.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "jason", null, "y\n");
    }

    public void testChangesWithRemoval() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithRemoval";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, null);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt checked in by author a\n");
        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt removed by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.DELETE, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt removed by author a\n");
        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt re-checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.ADD, "1.3");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt re-checked in by author a\n");
        changelist = changes.get(3);
        assertChangelistValues(changelist, "daniel", "file1.txt re-removed by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.DELETE, "1.4");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt re-removed by author a\n");
    }

    public void testChangesWithAdd() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithAdd";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, null);
        assertEquals(1, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and dir/file3.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", FileChange.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(2), "dir/file3.txt", FileChange.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt and file2.txt and dir/file3.txt checked in by author a\n");
    }

    public void testChangesWithModify() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithModify";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, null);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt checked in by author a\n");
        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt modified by author a\n");
        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.EDIT, "1.3");
        assertCvsRevision(changelist.getRevision(), "daniel", null, "file1.txt modified by author a\n");
    }

    public void testChangesWithBranch() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithBranch";
        CvsRevision fromRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, "BRANCH");
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file3.txt checked in on BRANCH by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file3.txt", FileChange.Action.ADD, "1.1.2.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "BRANCH", "file3.txt checked in on BRANCH by author a\n");
        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified on BRANCH by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", FileChange.Action.EDIT, "1.1.2.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "BRANCH", "file1.txt modified on BRANCH by author a\n");
    }

    public void testAddToBranchDoesNotAppearOnHead() throws ScmException, ParseException
    {
        // In testChangesWithBranch, file1 and file2 are added to head. We then branch, add file3
        // to the branch and edit file1.
        // a) expect 2 changes on branch - the add and the edit.
        // b) expect 1 change on head - initial add

        CvsRevision fromRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-12-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-12-30 00:00:00 GMT"));

        String module = "unit-test/CvsWorkerTest/testAddToBranchDoesNotAppearOnHead";

        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extractChangelists(infos, "BRANCH");
        assertEquals(1, changes.size());

        fromRevision.setBranch(null);
        toRevision.setBranch(null);

        infos = cvs.rlog(module, fromRevision, toRevision);
        changes = analyser.extractChangelists(infos, null);

        assertEquals(0, changes.size());
    }

    public void testChangesOnHeadAndBranch() throws ScmException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangesOnHeadAndBranch";

        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-10-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-10-18 00:00:00 GMT"));

        List<LogInformation> infos = cvs.rlog(module, fromRevision, toRevision);
        List<Changelist> changes = analyser.extractChangelists(infos, null);
        assertEquals(2, changes.size());


        fromRevision.setBranch("BRANCH");
        toRevision.setBranch("BRANCH");
        
        infos = cvs.rlog(module, fromRevision, toRevision);
        changes = analyser.extractChangelists(infos, "BRANCH");

        assertEquals(1, changes.size());
    }

    public void testGetLatestRevisionSince() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testGetLatestRevisionSince";

        CvsRevision since = new CvsRevision("", "", "", LOCAL_DATE.parse("2006-03-01 02:00:00"));
        Date latestUpdate = analyser.latestUpdate(cvs.rlog(module, since, null)).getDate();
        assertEquals("2006-03-10 04:00:00 GMT", SERVER_DATE.format(latestUpdate));

        since = new CvsRevision("", "", "", LOCAL_DATE.parse("2006-03-12 02:00:00"));
        assertNull(analyser.latestUpdate(cvs.rlog(module, since, null)));

        since = new CvsRevision("", "", "", SERVER_DATE.parse("2006-03-10 03:59:59 GMT"));
        latestUpdate = analyser.latestUpdate(cvs.rlog(module, since, null)).getDate();
        assertEquals("2006-03-10 04:00:00 GMT", SERVER_DATE.format(latestUpdate));

        since = new CvsRevision("", "", "", latestUpdate);
        Date otherLatest = analyser.latestUpdate(cvs.rlog(module, since, null)).getDate();
        assertEquals(latestUpdate, otherLatest);
    }

    private static void assertChangelistValues(Changelist changelist, String user, String comment)
    {
        assertEquals(user, changelist.getAuthor());
        assertEquals(comment, changelist.getComment());
    }

    private static void assertChangeValues(FileChange change, String file, FileChange.Action action, String revision)
    {
        StringAssertions.assertEndsWith(file, change.getPath());
        assertEquals(action, change.getAction());
        assertEquals(revision, change.getRevision().getRevisionString());
    }

    private static void assertValidChangeSets(List<Changelist> changelists)
    {
        for(Changelist changelist : changelists)
        {
            assertValidChangeSet(changelist);
        }
    }

    private static void assertValidChangeSet(Changelist changelist)
    {
        List<FileChange> changes = changelist.getChanges();
        Map<String, String> filenames = new HashMap<String, String>();

        for(FileChange change : changes)
        {
            assertFalse(filenames.containsKey(change.getPath()));
            filenames.put(change.getPath(), change.getPath());
            assertNotNull(change.getRevision().getRevisionString());
            assertNotNull(change.getAction());
        }
    }

    private static void assertCvsRevision(Revision revision, String author, String branch, String comment)
    {
        CvsRevision rev = CvsClient.convertRevision(revision);

        assertEquals(author, rev.getAuthor());
        assertEquals(branch, rev.getBranch());
    }

}
