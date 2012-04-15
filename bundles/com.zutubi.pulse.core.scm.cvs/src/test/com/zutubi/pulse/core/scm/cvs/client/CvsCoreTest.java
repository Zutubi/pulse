package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.cvs.CvsRevision;
import com.zutubi.pulse.core.scm.cvs.CvsTestUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.file.FileStatus;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class CvsCoreTest extends PulseTestCase
{
    private SimpleDateFormat serverDate;
    private CvsCore cvs;
    private File workdir;

    protected void setUp() throws Exception
    {
        super.setUp();

        serverDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        serverDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        String password = CvsTestUtils.getPassword("cvs-1.12.9");
        String cvsRoot = ":ext:cvs-1.12.9:" + password + "@zutubi.com:/cvsroots/default";
        cvs = new CvsCore();
        cvs.setRoot(CVSRoot.parse(cvsRoot));
        workdir = FileSystemUtils.createTempDir(CvsCoreTest.class.getName(), "");
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(workdir);
        super.tearDown();
    }

    public void testCheckoutHead() throws Exception
    {
        final String MODULE = "unit-test/CvsWorkerTest/testCheckout";
        final String[] FILES = {"file1.txt", "file2.txt", "dir1/file3.txt"};

        List<String> statuses = checkoutChanges(MODULE, CvsRevision.HEAD);
        assertEquals(FILES.length, statuses.size());

        for (int i = 0; i < FILES.length; i++)
        {
            File file = new File(workdir, MODULE + "/" + FILES[i]);
            assertTrue(file.exists());
            assertEquals("U " + MODULE + "/" + FILES[i], statuses.get(i));
        }

        assertTrue(new File(workdir, MODULE + "/dir2").exists());
    }

    public void testCheckoutByDate() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckout";
        CvsRevision byDate = new CvsRevision(null, null, null, toDate("2006-03-11 02:30:00 GMT"));
        List<String> statuses = checkoutChanges(module, byDate);
        assertEquals(0, statuses.size());
        assertNotExists(module + "/file1.txt");
        assertNotExists(module + "/file2.txt");
        assertNotExists(module + "/dir1/file3.txt");
    }

    public void testCheckoutModule() throws Exception
    {
        String module = "module";
        List<String> statuses = checkoutChanges(module, CvsRevision.HEAD);
        assertEquals(2, statuses.size());
        assertExists("unit-test/CvsWorkerTest/testCheckoutModule/dir1/file1.txt");
        assertExists("unit-test/CvsWorkerTest/testCheckoutModule/dir2/file2.txt");
    }

    public void testCheckoutBranch() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testCheckoutBranch";
        CvsRevision byBranch = new CvsRevision(null, "BRANCH", null, null);

        List changes = checkoutChanges(module, byBranch);
        assertExists(module + "/file1.txt");
        assertNotExists(module + "/file2.txt");
        assertExists(module + "/file3.txt");
        assertEquals(2, changes.size());

        byBranch.setBranch(null);
        cleanupCheckout();
        changes = checkoutChanges(module, byBranch);
        assertExists(module + "/file1.txt");
        assertExists(module + "/file2.txt");
        assertNotExists(module + "/file3.txt");
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
        CvsRevision byDate = new CvsRevision(null, null, toDate("2006-03-11 03:10:07 GMT"));
        String file = "unit-test/CvsWorkerTest/testCheckoutRevisionOfFile/file1.txt";
        cvs.checkout(workdir, file, byDate, null);
        assertContents("file1.txt revision 1.2 contents", new File(workdir, file));
    }

    public void testUpdateToHead() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testUpdateOnHead";
        CvsRevision byDate = new CvsRevision(null, null, toDate("2006-05-10 13:33:00 GMT"));
        cvs.checkout(workdir, module, byDate, null);
        assertNotExists(module + "/file1.txt");

        List changes = updateChanges(new File(workdir, module), CvsRevision.HEAD);
        assertExists(module + "/file1.txt");
        assertEquals(1, changes.size());
    }

    public void testUpdateToDate() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testUpdateOnHead";
        CvsRevision byDate = new CvsRevision(null, null, toDate("2006-05-10 13:33:00 GMT"));
        cvs.checkout(workdir, module, byDate, null);
        assertNotExists(module + "/file1.txt");
        byDate = new CvsRevision(null, null, toDate("2006-05-10 13:34:00 GMT"));
        List changes = updateChanges(new File(workdir, module), byDate);
        assertExists(module + "/file1.txt");
        assertEquals(1, changes.size());
    }

    /**
     * When updating, file edits should be returned in the list of changes.
     *
     * @throws Exception if an unexpected error occurs.
     */
    public void testUpdateWithEditsReceivesChange() throws Exception
    {
        final String MODULE = "unit-test/CvsWorkerTest/testUpdateWithEditsReceivesChange";
        final String FILENAME = "file.txt";
        final String FILEPATH = MODULE + "/" + FILENAME;

        CvsRevision byDate = new CvsRevision(null, null, toDate("2006-12-18 01:30:00 GMT"));
        cvs.checkout(workdir, MODULE, byDate, null);

        File x = new File(workdir, FILEPATH);
        assertFalse(x.exists());

        byDate = new CvsRevision(null, null, toDate("2006-12-19 01:31:00 GMT"));
        List<String> statuses = updateChanges(new File(workdir, MODULE), byDate);
        assertEquals(1, statuses.size());
        assertTrue(x.exists());
        assertEquals("some content", IOUtils.fileToString(x));
        assertEquals("U " + FILEPATH, statuses.get(0));

        byDate = new CvsRevision(null, null, toDate("2006-12-23 01:31:00 GMT"));
        statuses = updateChanges(new File(workdir, MODULE), byDate);
        assertTrue(x.exists());
        assertEquals(1, statuses.size());
        assertEquals("U " + FILEPATH, statuses.get(0));

        String fileContents = IOUtils.fileToString(x);
        assertTrue(fileContents.startsWith("some content"));
        assertTrue(fileContents.endsWith("Some more content"));
        assertEquals("U " + FILEPATH, statuses.get(0));

        byDate = new CvsRevision(null, null, toDate("2006-12-18 01:30:00 GMT"));
        statuses = updateChanges(new File(workdir, MODULE), byDate);
        assertFalse(x.exists());
        assertEquals(1, statuses.size());
        assertEquals("D " + FILEPATH, statuses.get(0));
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
        catch (ScmException e)
        {
        }

        cleanupCheckout();
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
        cleanupCheckout();
        cvs.deleteTag(module, tagName);
        byTag = new CvsRevision(null, tagName, null, null);
        cvs.checkout(workdir, module, byTag, null);
        assertFalse((new File(workdir, (new StringBuilder()).append(module).append("/file.txt").toString())).exists());
    }

    public void testRlog() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangeDetails";
        CvsRevision fromRevision = new CvsRevision(null, null, toDate("2006-03-10 00:59:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, toDate("2006-03-10 01:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(2, ((LogInformation) infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithRemoval() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithRemoval";
        CvsRevision fromRevision = new CvsRevision(null, null, toDate("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, toDate("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(4, ((LogInformation) infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithAdd() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithAdd";
        CvsRevision fromRevision = new CvsRevision(null, null, toDate("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, toDate("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(3, infos.size());
    }

    public void testRlogWithModify() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithModify";
        CvsRevision fromRevision = new CvsRevision(null, null, toDate("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, null, toDate("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(1, infos.size());
        assertEquals(3, ((LogInformation) infos.get(0)).getRevisionList().size());
    }

    public void testRlogWithBranch() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testChangesWithBranch";
        CvsRevision fromRevision = new CvsRevision(null, "BRANCH", null, toDate("2006-01-10 00:00:00 GMT"));
        CvsRevision toRevision = new CvsRevision(null, "BRANCH", null, toDate("2006-04-10 00:00:00 GMT"));
        List infos = cvs.rlog(module, fromRevision, toRevision);
        assertEquals(2, infos.size());
    }

    public void testStatus() throws Exception
    {
        String module = "unit-test/CvsWorkerTest/testStatus";
        cvs.checkout(workdir, module, CvsRevision.HEAD, null);
        List statuses = cvs.status(workdir);
        assertEquals(2, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation) statuses.get(0)).getStatus());
        assertEquals(FileStatus.UP_TO_DATE, ((StatusInformation) statuses.get(1)).getStatus());
        File fileTxt = new File(workdir, FileSystemUtils.composeFilename("unit-test", "CvsWorkerTest", "testStatus", "file.txt"));
        IOUtils.write(new Properties(), fileTxt);
        statuses = cvs.status(workdir);
        assertEquals(2, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation) statuses.get(0)).getStatus());
        assertEquals(FileStatus.MODIFIED, ((StatusInformation) statuses.get(1)).getStatus());
        assertTrue(fileTxt.delete());
        statuses = cvs.status(workdir);
        assertEquals(2, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation) statuses.get(0)).getStatus());
        assertEquals(FileStatus.NEEDS_CHECKOUT, ((StatusInformation) statuses.get(1)).getStatus());
        assertTrue((new File(fileTxt.getParentFile(), "new.file")).createNewFile());
        statuses = cvs.status(workdir);
        assertEquals(3, statuses.size());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation) statuses.get(0)).getStatus());
        assertEquals(FileStatus.UNKNOWN, ((StatusInformation) statuses.get(1)).getStatus());
        assertEquals(FileStatus.NEEDS_CHECKOUT, ((StatusInformation) statuses.get(2)).getStatus());
    }

    private List<String> checkoutChanges(String module, CvsRevision revision) throws ScmException
    {
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        cvs.checkout(workdir, module, revision, handler);
        return handler.getStatusMessages();
    }

    private List<String> updateChanges(File dir, CvsRevision revision) throws ScmException
    {
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        cvs.update(dir, revision, handler);
        return handler.getStatusMessages();
    }

    private void assertContents(String expected, File file) throws IOException
    {
        assertEquals(expected, IOUtils.fileToString(file).trim());
    }

    private Date toDate(String time) throws ParseException
    {
        return serverDate.parse(time);
    }

    private void assertNotExists(String path)
    {
        assertFalse(new File(workdir, path).exists());
    }

    private void assertExists(String path)
    {
        assertTrue(new File(workdir, path).exists());
    }

    private void cleanupCheckout() throws IOException
    {
        FileSystemUtils.cleanOutputDir(workdir);
    }
}
