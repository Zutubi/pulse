// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CvsClientTest.java

package com.zutubi.pulse.scm.cvs.client;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.file.FileStatus;
import org.netbeans.lib.cvsclient.util.Logger;

// Referenced classes of package com.zutubi.pulse.scm.cvs.client:
//            CvsClient, CvsException

public class CvsClientTest extends PulseTestCase
{

    public CvsClientTest()
    {
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
        Logger.setLogging("system");
        String cvsRoot = ":pserver:cvstester:cvs@www.cinnamonbob.com:/cvsroot";
        cvs = new CvsClient();
        cvs.setRoot(CVSRoot.parse(cvsRoot));
        workdir = FileSystemUtils.createTempDirectory(CvsClientTest.class.getName(), "");
    }

    protected void tearDown()
        throws Exception
    {
        removeDirectory(workdir);
        cvs = null;
        super.tearDown();
    }

    public void testCheckoutHead()
        throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckout";
        List changes = cvs.checkout(workdir, module, CvsRevision.HEAD);
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file2.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/dir1/file3.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/dir2").toString())).exists());
        assertEquals(3, changes.size());
        Change change;
        for(Iterator i$ = changes.iterator(); i$.hasNext(); assertEquals(com.zutubi.pulse.core.model.Change.Action.ADD, change.getAction()))
            change = (Change)i$.next();

    }

    public void testCheckoutByDate()
        throws SCMException, IOException, ParseException, CvsException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testCheckout";
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-11 02:30:00 GMT"));
        List changes = cvs.checkout(workdir, module, byDate);
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString())).exists());
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file2.txt").toString())).exists());
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/dir1/file3.txt").toString())).exists());
        assertEquals(0, changes.size());
    }

    public void testCheckoutModule()
        throws SCMException, CvsException, CommandException, AuthenticationException
    {
        String module = "module";
        List changes = cvs.checkout(workdir, module, CvsRevision.HEAD);
        assertTrue((new File(workdir, "unit-test/CvsWorkerTest/testCheckoutModule/dir1/file1.txt")).exists());
        assertTrue((new File(workdir, "unit-test/CvsWorkerTest/testCheckoutModule/dir2/file2.txt")).exists());
        assertEquals(2, changes.size());
        Change change;
        for(Iterator i$ = changes.iterator(); i$.hasNext(); assertEquals(com.zutubi.pulse.core.model.Change.Action.ADD, change.getAction()))
            change = (Change)i$.next();

    }

    public void testCheckoutBranch()
        throws SCMException, IOException, CvsException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testCheckoutBranch";
        CvsRevision byBranch = new CvsRevision(null, "BRANCH", null, null);
        List changes = cvs.checkout(workdir, module, byBranch);
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString())).exists());
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file2.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file3.txt").toString())).exists());
        assertEquals(2, changes.size());
        FileSystemUtils.cleanOutputDir(workdir);
        byBranch.setBranch(null);
        changes = cvs.checkout(workdir, module, byBranch);
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file2.txt").toString())).exists());
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file3.txt").toString())).exists());
        assertEquals(2, changes.size());
    }

    public void testCheckoutFileAtHead()
        throws Exception
    {
        String file = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile/file1.txt";
        List changes = cvs.checkout(workdir, file, CvsRevision.HEAD);
        assertContents("file1.txt latests contents", new File(workdir, file));
        assertEquals(1, changes.size());
    }

    public void testCheckoutFileByRevision()
        throws Exception
    {
        String file = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile/file1.txt";
        CvsRevision byRevision = new CvsRevision(null, "1.2", null, null);
        cvs.checkout(workdir, file, byRevision);
        assertContents("file1.txt revision 1.2 contents", new File(workdir, file));
        byRevision = new CvsRevision(null, "1.1", null, null);
        cvs.checkout(workdir, file, byRevision);
        assertContents("file1.txt revision 1.1 contents", new File(workdir, file));
    }

    public void testCheckoutFileByDate()
        throws Exception
    {
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-11 03:10:07 GMT"));
        String file = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile/file1.txt";
        cvs.checkout(workdir, file, byDate);
        assertContents("file1.txt revision 1.2 contents", new File(workdir, file));
    }

    public void testUpdateToHead()
        throws ParseException, SCMException, CvsException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testUpdateOnHead";
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:33:00 GMT"));
        cvs.checkout(workdir, module, byDate);
        File x = new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString());
        assertFalse(x.exists());
        List changes = cvs.update(new File(workdir, module), CvsRevision.HEAD);
        assertTrue(x.exists());
        assertEquals(1, changes.size());
    }

    public void testUpdateToDate()
        throws SCMException, ParseException, CvsException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testUpdateOnHead";
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:33:00 GMT"));
        cvs.checkout(workdir, module, byDate);
        File x = new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString());
        assertFalse(x.exists());
        byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:34:00 GMT"));
        List changes = cvs.update(new File(workdir, module), byDate);
        assertTrue(x.exists());
        assertEquals(1, changes.size());
    }

    public void testTagContent()
        throws SCMException, IOException, CvsException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testTagContent";
        File baseCheckoutDir = new File(workdir, module);
        String tagName = (new StringBuilder()).append("T_").append(String.valueOf(System.currentTimeMillis())).toString();
        CvsRevision tag = new CvsRevision(null, tagName, null, null);
        try
        {
            cvs.checkout(workdir, module, tag);
            fail();
        }
        catch(SCMException e) { }
        FileSystemUtils.cleanOutputDir(workdir);
        cvs.tag(module, CvsRevision.HEAD, tagName, false);
        cvs.checkout(workdir, module, tag);
        assertTrue((new File(baseCheckoutDir, "file.txt")).exists());
    }

    public void testDeleteTag()
        throws SCMException, IOException, CvsException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testTagContent";
        String tagName = (new StringBuilder()).append("T_").append(String.valueOf(System.currentTimeMillis())).toString();
        cvs.tag(module, CvsRevision.HEAD, tagName, false);
        CvsRevision byTag = new CvsRevision(null, tagName, null, null);
        cvs.checkout(workdir, module, byTag);
        FileSystemUtils.cleanOutputDir(workdir);
        cvs.deleteTag(module, tagName);
        byTag = new CvsRevision(null, tagName, null, null);
        cvs.checkout(workdir, module, byTag);
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file.txt").toString())).exists());
    }

    public void testRlog()
        throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 01:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(2, ((LogInformation)infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithRemoval()
        throws SCMException, ParseException, CommandException, AuthenticationException
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithRemoval";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(4, ((LogInformation)infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithAdd()
        throws SCMException, CommandException, AuthenticationException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithAdd";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(3, infos.size());
    }

    public void testRlogWithModify()
        throws SCMException, CommandException, AuthenticationException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithModify";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(3, ((LogInformation)infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithBranch()
        throws SCMException, CommandException, AuthenticationException, ParseException
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithBranch";
        CvsRevision fromRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(2, infos.size());
    }

    public void testStatus()
        throws SCMException, IOException
    {
        String module = "unit-test/CvsWorkerTest/testStatus";
        cvs.checkout(workdir, module, CvsRevision.HEAD);
        List statuses = cvs.status(workdir);
        assertEquals(2, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation)statuses.get(0)).getStatus());
        assertEquals(FileStatus.UP_TO_DATE, ((StatusInformation)statuses.get(1)).getStatus());
        File fileTxt = new File(workdir, FileSystemUtils.composeFilename(new String[] {
            "unit-test", "CvsWorkerTest", "testStatus", "file.txt"
        }));
        IOUtils.write(new Properties(), fileTxt);
        statuses = cvs.status(workdir);
        assertEquals(2, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation)statuses.get(0)).getStatus());
        assertEquals(FileStatus.MODIFIED, ((StatusInformation)statuses.get(1)).getStatus());
        assertTrue(fileTxt.delete());
        statuses = cvs.status(workdir);
        assertEquals(2, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation)statuses.get(0)).getStatus());
        assertEquals(FileStatus.NEEDS_CHECKOUT, ((StatusInformation)statuses.get(1)).getStatus());
        assertTrue((new File(fileTxt.getParentFile(), "new.file")).createNewFile());
        statuses = cvs.status(workdir);
        assertEquals(3, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation)statuses.get(0)).getStatus());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation)statuses.get(1)).getStatus());
        assertEquals(FileStatus.NEEDS_CHECKOUT, ((StatusInformation)statuses.get(2)).getStatus());
    }

    private void assertContents(String expected, File file)
        throws IOException
    {
        assertEquals(expected, IOUtils.fileToString(file).trim());
    }

    private static final SimpleDateFormat LOCAL_DATE;
    private static final SimpleDateFormat SERVER_DATE;
    private CvsClient cvs;
    private File workdir;

    static 
    {
        LOCAL_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LOCAL_DATE.setTimeZone(TimeZone.getTimeZone("EST"));
        SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
}
