package com.zutubi.pulse.scm.svn;

import com.zutubi.pulse.config.PropertiesConfig;
import com.zutubi.pulse.scm.FileStatus;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.WorkingCopyStatus;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import java.io.File;
import java.io.FileInputStream;

/**
 */
public class SvnWorkingCopyTest extends PulseTestCase
{
    private File tempDir;
    private Process svnProcess;
    private File base;
    private SVNClientManager clientManager;
    private SVNUpdateClient updateClient;
    private File otherBase;
    private SVNUpdateClient otherUpdateClient;

    private SvnWorkingCopy wc;
    private SVNWCClient client;

    static
    {
        // Initialise SVN library
        SVNRepositoryFactoryImpl.setup();
    }

    //    jsankey@shiny:~/temp/wc$ ls -R
    //    .:
    //    bin1  dir2   file2  file4     macfile2  script2    textfile2  unixfile2  winfile2
    //    dir1  file1  file3  macfile1  script1   textfile1  unixfile1  winfile1
    //
    //    ./dir1:
    //    file1  file2  file3
    //
    //    ./dir2:
    //    file1  file2


    protected void setUp() throws Exception
    {
        tempDir = FileSystemUtils.createTempDirectory(getName(), "");
        tempDir = tempDir.getCanonicalFile();

        // Create empty repo
        File repoDir = new File(tempDir, "repo");
        repoDir.mkdir();
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnadmin", "create", repoDir.getAbsolutePath() });
        svnProcess.waitFor();

        // Allow anonymous writes
        File conf = new File(repoDir, FileSystemUtils.composeFilename("conf", "svnserve.conf"));
        if(!conf.exists())
        {
            throw new RuntimeException("i am teh stoopid");
        }
        FileSystemUtils.createFile(conf, "[general]\nanon-access = write\nauth-access = write\n");

        // Restore from dump
        File repoZip = getTestDataFile("core", "repo", "zip");
        FileSystemUtils.extractZip(repoZip, tempDir);

        File dump = new File(tempDir, "SvnWorkingCopyTest.dump");
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnadmin", "load", repoDir.getAbsolutePath() });
        FileInputStream is = new FileInputStream(dump);
        IOUtils.joinStreams(is, svnProcess.getOutputStream());
        is.close();
        svnProcess.getOutputStream().close();
        svnProcess.waitFor();

        // Start svn server
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnserve", "--foreground", "-dr", repoDir.getAbsolutePath()});
        waitForServer(3690);

        clientManager = SVNClientManager.newInstance();
        createWC();
    }

    private void createWC() throws SCMException, SVNException
    {
        base = new File(tempDir, "base");
        base.mkdir();
        updateClient = new SVNUpdateClient(new BasicAuthenticationManager("anonymous", ""), clientManager.getOptions());
        updateClient.doCheckout(SVNURL.parseURIDecoded("svn://localhost/test/trunk"), base, SVNRevision.UNDEFINED, SVNRevision.HEAD, true);
        client = clientManager.getWCClient();
        wc = new SvnWorkingCopy(base, new PropertiesConfig());
    }

    private void createOtherWC() throws SVNException
    {
        otherBase = new File(tempDir, "other");
        otherBase.mkdir();
        otherUpdateClient = new SVNUpdateClient(new BasicAuthenticationManager("anonymous", ""), clientManager.getOptions());
        otherUpdateClient.doCheckout(SVNURL.parseURIDecoded("svn://localhost/test/trunk"), otherBase, SVNRevision.UNDEFINED, SVNRevision.HEAD, true);
    }

    protected void tearDown() throws Exception
    {
        svnProcess.destroy();
        svnProcess.waitFor();
        svnProcess = null;
        Thread.sleep(100);

        FileSystemUtils.removeDirectory(tempDir);

        updateClient = null;
        otherUpdateClient = null;
        client = null;
        wc = null;
    }

    public void testGetStatusNoChanges() throws Exception
    {
        WorkingCopyStatus wcs = wc.getStatus();
        assertEquals("4", wcs.getRevision().getRevisionString());
        assertEquals(0, wcs.getChanges().size());
    }

    public void testGetStatusEdited() throws Exception
    {
        File test = new File(base, "file1");
        FileSystemUtils.createFile(test, "hello");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, false);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusEditedText() throws Exception
    {
        File test = new File(base, "textfile1");
        FileSystemUtils.createFile(test, "hello");
        WorkingCopyStatus wcs = assertSimpleStatus("textfile1", FileStatus.State.MODIFIED, false);
        assertEOL(wcs, "textfile1", FileStatus.EOLStyle.NATIVE);
    }

    public void testGetStatusEditedNewlyText() throws Exception
    {
        File test = new File(base, "file1");
        FileSystemUtils.createFile(test, "hello");
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EOL_STYLE, "native", true, false, null);

        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, false);
        assertEOL(wcs, "file1", FileStatus.EOLStyle.NATIVE);
    }

    public void testGetStatusEditedLF() throws Exception
    {
        File test = new File(base, "unixfile1");
        FileSystemUtils.createFile(test, "hello");
        WorkingCopyStatus wcs = assertSimpleStatus("unixfile1", FileStatus.State.MODIFIED, false);
        assertEOL(wcs, "unixfile1", FileStatus.EOLStyle.LINEFEED);
    }

    public void testGetStatusEditedAddedRandomProperty() throws Exception
    {
        File test = new File(base, "file1");
        FileSystemUtils.createFile(test, "hello");
        client.doSetProperty(test, "random", "value", true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, false);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusEditedAddedExecutableProperty() throws Exception
    {
        File test = new File(base, "file1");
        FileSystemUtils.createFile(test, "hello");
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EXECUTABLE, "yay", true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, false);
        assertExecutable(wcs, "file1", true);
    }

    public void testGetStatusEditedRemovedExecutableProperty() throws Exception
    {
        File test = new File(base, "bin1");
        FileSystemUtils.createFile(test, "hello");
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EXECUTABLE, null, true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("bin1", FileStatus.State.MODIFIED, false);
        assertExecutable(wcs, "bin1", false);
    }

    public void testGetStatusAdded() throws Exception
    {
        File test = new File(base, "newfile");
        FileSystemUtils.createFile(test, "hello");

        client.doAdd(test, true, false, false, false);
        WorkingCopyStatus wcs = assertSimpleStatus("newfile", FileStatus.State.ADDED, false);
        assertNoProperties(wcs, "newfile");
    }

    public void testGetStatusAddedText() throws Exception
    {
        File test = new File(base, "newfile");
        FileSystemUtils.createFile(test, "hello");

        client.doAdd(test, true, false, false, false);
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EOL_STYLE, "native", true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("newfile", FileStatus.State.ADDED, false);
        assertEOL(wcs, "newfile", FileStatus.EOLStyle.NATIVE);
    }

    public void testGetStatusDeleted() throws Exception
    {
        File test = new File(base, "file1");

        client.doDelete(test, false, false);
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.DELETED, false);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusUnchangedOOD() throws Exception
    {
        createOtherWC();
        File test = new File(otherBase, "file1");
        FileSystemUtils.createFile(test, "hello");
        clientManager.getCommitClient().doCommit(new File[] { test }, true, "edit file", false, false);

        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.UNCHANGED, true);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusEditedOOD() throws Exception
    {
        createOtherWC();
        File test = new File(otherBase, "file1");
        FileSystemUtils.createFile(test, "hello");
        clientManager.getCommitClient().doCommit(new File[] { test }, true, "edit file", false, false);

        test = new File(base, "file1");
        FileSystemUtils.createFile(test, "hello");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, true);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusEditDeleted() throws Exception
    {
        createOtherWC();
        File test = new File(otherBase, "file1");
        client.doDelete(test, false, false);
        clientManager.getCommitClient().doCommit(new File[] { test }, true, "delete file", false, false);

        test = new File(base, "file1");
        FileSystemUtils.createFile(test, "hello");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, true);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusDeleteEdited() throws Exception
    {
        createOtherWC();
        File test = new File(otherBase, "file1");
        FileSystemUtils.createFile(test, "hello");
        clientManager.getCommitClient().doCommit(new File[] { test }, true, "edit file", false, false);

        test = new File(base, "file1");
        client.doDelete(test, false, false);
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.DELETED, true);
        assertNoProperties(wcs, "file1");
    }

    public void testUpdateAlreadyUpToDate() throws Exception
    {
        wc.update();
        testGetStatusNoChanges();
    }

    public void testUpdateBasic() throws Exception
    {
        createOtherWC();
        File test = new File(otherBase, "file1");
        FileSystemUtils.createFile(test, "hello");
        clientManager.getCommitClient().doCommit(new File[] { test }, true, "edit file", false, false);

        wc.update();
        WorkingCopyStatus wcs = wc.getStatus();
        assertEquals("5", wcs.getRevision().getRevisionString());
        assertEquals(0, wcs.getChanges().size());
    }

    public void testUpdateConflict() throws Exception
    {
        createOtherWC();
        File test = new File(otherBase, "file1");
        FileSystemUtils.createFile(test, "hello");
        clientManager.getCommitClient().doCommit(new File[] { test }, true, "edit file", false, false);

        test = new File(base, "file1");
        FileSystemUtils.createFile(test, "goodbye");
        wc.update();
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.UNRESOLVED, false);
        assertEquals("5", wcs.getRevision().getRevisionString());
    }

    private WorkingCopyStatus assertSimpleStatus(String path, FileStatus.State state, boolean ood) throws SCMException
    {
        WorkingCopyStatus wcs = wc.getStatus();
        assertEquals(1, wcs.getChanges().size());
        FileStatus fs = wcs.getFileStatus(path);
        assertEquals(state, fs.getState());
        assertFalse(fs.isDirectory());
        assertEquals(ood, fs.isOutOfDate());
        return wcs;
    }

    private void assertNoProperties(WorkingCopyStatus wcs, String path)
    {
        assertEquals(0, wcs.getFileStatus(path).getProperties().size());
    }

    private void assertEOL(WorkingCopyStatus wcs, String path, FileStatus.EOLStyle eol)
    {
        FileStatus fs = wcs.getFileStatus(path);
        assertEquals(eol.toString(), fs.getProperty(FileStatus.PROPERTY_EOL_STYLE));
    }

    private void assertExecutable(WorkingCopyStatus wcs, String path, boolean executable)
    {
        FileStatus fs = wcs.getFileStatus(path);
        assertEquals(Boolean.toString(executable), fs.getProperty(FileStatus.PROPERTY_EXECUTABLE));
    }
}
