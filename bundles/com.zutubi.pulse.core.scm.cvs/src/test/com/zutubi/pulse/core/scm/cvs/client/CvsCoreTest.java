package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.pulse.core.scm.ScmChangeAccumulator;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.cvs.CvsRevision;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.file.FileStatus;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class CvsCoreTest extends PulseTestCase
{
    private static final SimpleDateFormat SERVER_DATE;
    private CvsCore cvs;
    private File workdir;

    static
    {
        SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public CvsCoreTest()
    {
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        Logger.setLogging("system");
        
        String cvsRoot = ":ext:daniel:xxxx@zutubi.com:/cvsroots/default";
        cvs = new CvsCore();
        cvs.setRoot(CVSRoot.parse(cvsRoot));
        workdir = FileSystemUtils.createTempDir(CvsCoreTest.class.getName(), "");
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(workdir);
        cvs = null;
        workdir = null;
        super.tearDown();
    }

    public void testCheckoutHead() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckout";
        List<FileChange> changes = checkoutChanges(module, CvsRevision.HEAD);
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file2.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/dir1/file3.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/dir2").toString())).exists());
        assertEquals(3, changes.size());

        for (FileChange change : changes)
        {
            assertEquals(FileChange.Action.ADD, change.getAction());
        }
    }

    public void testCheckoutByDate() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckout";
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-11 02:30:00 GMT"));
        List changes = checkoutChanges(module, byDate);
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString())).exists());
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file2.txt").toString())).exists());
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/dir1/file3.txt").toString())).exists());
        assertEquals(0, changes.size());
    }

    public void testCheckoutModule() throws Exception
    {
        String module = "module";
        List<FileChange> changes = checkoutChanges(module, CvsRevision.HEAD);
        assertTrue((new File(workdir, "unit-test/CvsWorkerTest/testCheckoutModule/dir1/file1.txt")).exists());
        assertTrue((new File(workdir, "unit-test/CvsWorkerTest/testCheckoutModule/dir2/file2.txt")).exists());
        assertEquals(2, changes.size());

        for (FileChange change : changes)
        {
            assertEquals(FileChange.Action.ADD, change.getAction());
        }
    }

    public void testCheckoutBranch() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckoutBranch";
        CvsRevision byBranch = new CvsRevision(null, "BRANCH", null, null);
        List changes = checkoutChanges(module, byBranch);
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString())).exists());
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file2.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file3.txt").toString())).exists());
        assertEquals(2, changes.size());
        FileSystemUtils.cleanOutputDir(workdir);
        byBranch.setBranch(null);
        changes = checkoutChanges(module, byBranch);
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString())).exists());
        assertTrue((new File(workdir, (new StringBuilder()).append(module).append("/file2.txt").toString())).exists());
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file3.txt").toString())).exists());
        assertEquals(2, changes.size());
    }

    public void testCheckoutFileAtHead() throws Exception
    {
        String file = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile/file1.txt";
        List changes = checkoutChanges(file, CvsRevision.HEAD);
        assertContents("file1.txt latests contents", new File(workdir, file));
        assertEquals(1, changes.size());
    }

    public void testCheckoutFileByRevision() throws Exception
    {
        String file = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile/file1.txt";
        CvsRevision byRevision = new CvsRevision(null, "1.2", null, null);
        cvs.checkout(workdir, file, byRevision, null);
        assertContents("file1.txt revision 1.2 contents", new File(workdir, file));
        byRevision = new CvsRevision(null, "1.1", null, null);
        cvs.checkout(workdir, file, byRevision, null);
        assertContents("file1.txt revision 1.1 contents", new File(workdir, file));
    }

    public void testCheckoutFileByDate() throws Exception
    {
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-11 03:10:07 GMT"));
        String file = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile/file1.txt";
        cvs.checkout(workdir, file, byDate, null);
        assertContents("file1.txt revision 1.2 contents", new File(workdir, file));
    }

    public void testUpdateToHead() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testUpdateOnHead";
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:33:00 GMT"));
        cvs.checkout(workdir, module, byDate, null);
        File x = new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString());
        assertFalse(x.exists());

        List changes = updateChanges(new File(workdir, module), CvsRevision.HEAD);
        assertTrue(x.exists());
        assertEquals(1, changes.size());
    }

    public void testUpdateToDate() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testUpdateOnHead";
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:33:00 GMT"));
        cvs.checkout(workdir, module, byDate, null);
        File x = new File(workdir, (new StringBuilder()).append(module).append("/file1.txt").toString());
        assertFalse(x.exists());
        byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-05-10 13:34:00 GMT"));
        List changes = updateChanges(new File(workdir, module), byDate);
        assertTrue(x.exists());
        assertEquals(1, changes.size());
    }

    /**
     * When updating, file edits should be returned in the list of changes.
     *
     * @throws Exception if an unexpected error occurs.
     */
    public void testUpdateWithEditsReceivesChange() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testUpdateWithEditsReceivesChange";
        CvsRevision byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-12-18 01:30:00 GMT"));
        cvs.checkout(workdir, module, byDate, null);

        File x = new File(workdir, StringUtils.join("/", module, "file.txt"));
        assertFalse(x.exists());

        byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-12-19 01:31:00 GMT"));
        List<FileChange> changes = updateChanges(new File(workdir, module), byDate);
        assertEquals(1, changes.size());
        assertTrue(x.exists());
        assertEquals("some content", IOUtils.fileToString(x));
        assertEquals( FileChange.Action.EDIT, changes.get(0).getAction());

        byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-12-23 01:31:00 GMT"));
        changes = updateChanges(new File(workdir, module), byDate);
        assertTrue(x.exists());
        assertEquals(1, changes.size());
        assertEquals( FileChange.Action.EDIT, changes.get(0).getAction());

        String fileContents = IOUtils.fileToString(x);
        assertTrue(fileContents.startsWith("some content"));
        assertTrue(fileContents.endsWith("Some more content"));
        assertEquals( FileChange.Action.EDIT, changes.get(0).getAction());

        byDate = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-12-18 01:30:00 GMT"));
        changes = updateChanges(new File(workdir, module), byDate);
        assertFalse(x.exists());
        assertEquals(1, changes.size());
        assertEquals( FileChange.Action.DELETE, changes.get(0).getAction());
    }

    public void testTagContent() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testTagContent";
        File baseCheckoutDir = new File(workdir, module);
        String tagName = (new StringBuilder()).append("T_").append(String.valueOf(System.currentTimeMillis())).toString();
        CvsRevision tag = new CvsRevision(null, tagName, null, null);
        try
        {
            cvs.checkout(workdir, module, tag, null);
            fail();
        }
        catch(ScmException e)
        { 
        }

        FileSystemUtils.cleanOutputDir(workdir);
        cvs.tag(module, CvsRevision.HEAD, tagName, false);
        cvs.checkout(workdir, module, tag, null);
        assertTrue((new File(baseCheckoutDir, "file.txt")).exists());
    }

    public void testDeleteTag() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testTagContent";
        String tagName = (new StringBuilder()).append("T_").append(String.valueOf(System.currentTimeMillis())).toString();
        cvs.tag(module, CvsRevision.HEAD, tagName, false);
        CvsRevision byTag = new CvsRevision(null, tagName, null, null);
        cvs.checkout(workdir, module, byTag, null);
        FileSystemUtils.cleanOutputDir(workdir);
        cvs.deleteTag(module, tagName);
        byTag = new CvsRevision(null, tagName, null, null);
        cvs.checkout(workdir, module, byTag, null);
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file.txt").toString())).exists());
    }

    public void testRlog() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-03-10 01:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(2, ((LogInformation)infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithRemoval() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithRemoval";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(4, ((LogInformation)infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithAdd() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithAdd";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(3, infos.size());
    }

    public void testRlogWithModify() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithModify";
        CvsRevision fromRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(3, ((LogInformation)infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithBranch() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithBranch";
        CvsRevision fromRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, "BRANCH", null, SERVER_DATE.parse("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(2, infos.size());
    }

    public void testStatus() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testStatus";
        cvs.checkout(workdir, module, CvsRevision.HEAD, null);
        List statuses = cvs.status(workdir);
        assertEquals(2, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation)statuses.get(0)).getStatus());
        assertEquals(FileStatus.UP_TO_DATE, ((StatusInformation)statuses.get(1)).getStatus());
        File fileTxt = new File(workdir, FileSystemUtils.composeFilename("unit-test", "CvsWorkerTest", "testStatus", "file.txt"));
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

    private List<FileChange> checkoutChanges(String module, CvsRevision revision) throws ScmException
    {
        ScmChangeAccumulator accumulator = new ScmChangeAccumulator();
        cvs.checkout(workdir, module, revision, accumulator);
        return accumulator.getChanges();
    }

    private List<FileChange> updateChanges(File dir, CvsRevision revision) throws ScmException
    {
        ScmChangeAccumulator accumulator = new ScmChangeAccumulator();
        cvs.update(dir, revision, accumulator);
        return accumulator.getChanges();
    }

    private void assertContents(String expected, File file)
        throws IOException
    {
        assertEquals(expected, IOUtils.fileToString(file).trim());
    }

}
