// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LogInformationAnalyserTest.java

package com.zutubi.pulse.scm.cvs.client;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.PulseTestCase;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.util.Logger;

// Referenced classes of package com.zutubi.pulse.scm.cvs.client:
//            CvsClient, LogInformationAnalyser

public class LogInformationAnalyserTest extends PulseTestCase
{

    public LogInformationAnalyserTest()
    {
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
        Logger.setLogging("system");
        String cvsRoot = ":ext:cvstester:cvs@www.cinnamonbob.com:/cvsroot";
        cvs = new CvsClient();
        cvs.setRoot(CVSRoot.parse(cvsRoot));
        analyser = new LogInformationAnalyser("test", CVSRoot.parse(cvsRoot));
    }

    public void testGetChangesBetween()
        throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 01:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, null);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = (Changelist)changes.get(0);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt modified by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");
        Change change = (Change)changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(com.zutubi.pulse.core.model.Change.Action.EDIT, change.getAction());
        assertEquals("1.2", change.getRevision());
        changelist = (Changelist)changes.get(1);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt deleted by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt deleted by author a\n");
        change = (Change)changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(com.zutubi.pulse.core.model.Change.Action.DELETE, change.getAction());
        assertEquals("1.3", change.getRevision());
    }

    public void testChangeDetails()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-02-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 01:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, null);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = (Changelist)changes.get(0);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");
        Change change = (Change)changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(com.zutubi.pulse.core.model.Change.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision());
        changelist = (Changelist)changes.get(1);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt modified by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");
        change = (Change)changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(com.zutubi.pulse.core.model.Change.Action.EDIT, change.getAction());
        assertEquals("1.2", change.getRevision());
        changelist = (Changelist)changes.get(2);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt deleted by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt deleted by author a\n");
        change = (Change)changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(com.zutubi.pulse.core.model.Change.Action.DELETE, change.getAction());
        assertEquals("1.3", change.getRevision());
        changelist = (Changelist)changes.get(3);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file2.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file2.txt checked in by author a\n");
        change = (Change)changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsWorkerTest/testChangeDetails/file2.txt", change.getFilename());
        assertEquals(com.zutubi.pulse.core.model.Change.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision());
    }

    public void testChangesByDifferentAuthors()
        throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesByDifferentAuthors";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, null);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = (Changelist)changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertEquals(1, changelist.getChanges().size());
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");
        changelist = (Changelist)changes.get(1);
        assertChangelistValues(changelist, "jason", "file2.txt checked in by author b\n");
        assertEquals(1, changelist.getChanges().size());
        assertChangeValues((Change)changelist.getChanges().get(0), "file2.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "jason", "", "file2.txt checked in by author b\n");
    }

    public void testChangesByOverlappingCommits()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangesByOverlappingCommits";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, null);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = (Changelist)changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and file3.txt and file4.txt are checked in by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertChangeValues((Change)changelist.getChanges().get(1), "file2.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertChangeValues((Change)changelist.getChanges().get(2), "file3.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertChangeValues((Change)changelist.getChanges().get(3), "file4.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt and file2.txt and file3.txt and file4.txt are checked in by author a\n");
        changelist = (Changelist)changes.get(1);
        assertChangelistValues(changelist, "daniel", "x\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.EDIT, "1.2");
        assertChangeValues((Change)changelist.getChanges().get(1), "file3.txt", com.zutubi.pulse.core.model.Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "x\n");
        changelist = (Changelist)changes.get(2);
        assertChangelistValues(changelist, "jason", "y\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file2.txt", com.zutubi.pulse.core.model.Change.Action.EDIT, "1.2");
        assertChangeValues((Change)changelist.getChanges().get(1), "file4.txt", com.zutubi.pulse.core.model.Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "jason", "", "y\n");
    }

    public void testChangesWithRemoval()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithRemoval";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, null);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = (Changelist)changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");
        changelist = (Changelist)changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt removed by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.DELETE, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt removed by author a\n");
        changelist = (Changelist)changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt re-checked in by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.3");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt re-checked in by author a\n");
        changelist = (Changelist)changes.get(3);
        assertChangelistValues(changelist, "daniel", "file1.txt re-removed by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.DELETE, "1.4");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt re-removed by author a\n");
    }

    public void testChangesWithAdd()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithAdd";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, null);
        assertEquals(1, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = (Changelist)changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and dir/file3.txt checked in by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertChangeValues((Change)changelist.getChanges().get(1), "file2.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertChangeValues((Change)changelist.getChanges().get(2), "dir/file3.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt and file2.txt and dir/file3.txt checked in by author a\n");
    }

    public void testChangesWithModify()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithModify";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, null);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = (Changelist)changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt checked in by author a\n");
        changelist = (Changelist)changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.EDIT, "1.2");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");
        changelist = (Changelist)changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.EDIT, "1.3");
        assertCvsRevision(changelist.getRevision(), "daniel", "", "file1.txt modified by author a\n");
    }

    public void testChangesWithBranch()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithBranch";
        CvsRevision fromRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, "BRANCH");
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);
        Changelist changelist = (Changelist)changes.get(0);
        assertChangelistValues(changelist, "daniel", "file3.txt checked in on BRANCH by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file3.txt", com.zutubi.pulse.core.model.Change.Action.ADD, "1.1.2.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "BRANCH", "file3.txt checked in on BRANCH by author a\n");
        changelist = (Changelist)changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified on BRANCH by author a\n");
        assertChangeValues((Change)changelist.getChanges().get(0), "file1.txt", com.zutubi.pulse.core.model.Change.Action.EDIT, "1.1.2.1");
        assertCvsRevision(changelist.getRevision(), "daniel", "BRANCH", "file1.txt modified on BRANCH by author a\n");
    }

    public void testChangesOnHeadAndBranch()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangesOnHeadAndBranch";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-10-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-10-18 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        List changes = analyser.extract(infos, null);
        assertEquals(2, changes.size());
        Changelist changelist = (Changelist)changes.get(0);
        assertEquals("", changelist.getRevision().getBranch());
        changelist = (Changelist)changes.get(1);
        assertEquals("", changelist.getRevision().getBranch());
        fromRevision.setBranch("BRANCH");
        toRevision.setBranch("BRANCH");
        infos = cvs.rlog(module, fromRevision, toRevision);
        changes = analyser.extract(infos, "BRANCH");
        assertEquals(1, changes.size());
        changelist = (Changelist)changes.get(0);
        assertEquals("BRANCH", changelist.getRevision().getBranch());
    }

    public void testGetLatestRevisionSince()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testGetLatestRevisionSince";
        CvsRevision since = new CvsRevision("", "", "", LOCAL_DATE.parse("2006-03-01 02:00:00"));
        java.util.Date latestUpdate = analyser.latestUpdate(cvs.rlog(module, since, null));
        assertEquals("2006-03-10 04:00:00 GMT", SERVER_DATE.format(latestUpdate));
        since = new CvsRevision("", "", "", LOCAL_DATE.parse("2006-03-12 02:00:00"));
        latestUpdate = analyser.latestUpdate(cvs.rlog(module, since, null));
        assertNull(latestUpdate);
        since = new CvsRevision("", "", "", SERVER_DATE.parse("2006-03-10 03:59:59 GMT"));
        latestUpdate = analyser.latestUpdate(cvs.rlog(module, since, null));
        assertEquals("2006-03-10 04:00:00 GMT", SERVER_DATE.format(latestUpdate));
        since = new CvsRevision("", "", "", latestUpdate);
        java.util.Date otherLatest = analyser.latestUpdate(cvs.rlog(module, since, null));
        assertEquals(latestUpdate, otherLatest);
    }

    private static void assertChangelistValues(Changelist changelist, String user, String comment)
    {
        assertEquals(user, changelist.getUser());
        assertEquals(comment, changelist.getComment());
    }

    private static void assertChangeValues(Change change, String file, com.zutubi.pulse.core.model.Change.Action action, String revision)
    {
        assertEndsWith(file, change.getFilename());
        assertEquals(action, change.getAction());
        assertEquals(revision, change.getRevision());
    }

    private static void assertValidChangeSets(List changelists)
    {
        Changelist changelist;
        for(Iterator i$ = changelists.iterator(); i$.hasNext(); assertValidChangeSet(changelist))
            changelist = (Changelist)i$.next();

    }

    private static void assertValidChangeSet(Changelist changelist)
    {
        List changes = changelist.getChanges();
        Map filenames = new HashMap();
        Change change;
        for(Iterator i$ = changes.iterator(); i$.hasNext(); assertNotNull(change.getAction()))
        {
            change = (Change)i$.next();
            assertFalse(filenames.containsKey(change.getFilename()));
            filenames.put(change.getFilename(), change.getFilename());
            assertNotNull(change.getRevision());
        }

    }

    private static void assertCvsRevision(Revision revision, String author, String branch, String comment)
    {
        CvsRevision cvsRev = (CvsRevision)revision;
        assertEquals(author, cvsRev.getAuthor());
        assertEquals(branch, cvsRev.getBranch());
        assertEquals(comment, cvsRev.getComment());
    }

    private static final SimpleDateFormat LOCAL_DATE;
    private static final SimpleDateFormat SERVER_DATE;
    private CvsClient cvs;
    private LogInformationAnalyser analyser;

    static 
    {
        LOCAL_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LOCAL_DATE.setTimeZone(TimeZone.getTimeZone("EST"));
        SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
}
