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
import java.io.IOException;
import java.util.Properties;

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
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnadmin", "load", "-q", repoDir.getAbsolutePath() });
        FileInputStream is = new FileInputStream(dump);
        IOUtils.joinStreams(is, svnProcess.getOutputStream());
        svnProcess.getOutputStream().close();
        is.close();
        svnProcess.waitFor();

        // Start svn server
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnserve", "--foreground", "-dr", "."}, null, repoDir);
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

    public void testMatchesRepositoryMatches() throws SCMException
    {
        Properties p = new Properties();
        p.put(SvnConstants.PROPERTY_URL, "svn://localhost/test/trunk");
        assertTrue(wc.matchesRepository(p));
    }

    public void testMatchesRepositoryDoesntMatch() throws SCMException
    {
        Properties p = new Properties();
        p.put(SvnConstants.PROPERTY_URL, "svn://localhost/test/branches/1.0.x");
        assertFalse(wc.matchesRepository(p));
    }

    public void testMatchesRepositoryEmbeddedUser() throws SCMException
    {
        Properties p = new Properties();
        p.put(SvnConstants.PROPERTY_URL, "svn://goober@localhost/test/trunk");
        assertTrue(wc.matchesRepository(p));
    }

    public void testGetStatusNoChanges() throws Exception
    {
        WorkingCopyStatus wcs = wc.getStatus();
        assertEquals("4", wcs.getRevision().getRevisionString());
        assertEquals(0, wcs.getChanges().size());
    }

    public void testGetLocalStatusNoChanges() throws Exception
    {
        WorkingCopyStatus wcs = wc.getLocalStatus();
        assertEquals(0, wcs.getChanges().size());
    }

    public void testGetStatusEdited() throws Exception
    {
        getStatusEdited(false);
    }

    public void testGetLocalStatusEdited() throws Exception
    {
        getStatusEdited(true);
    }

    private void getStatusEdited(boolean remote) throws IOException, SCMException
    {
        edit("file1");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, false, remote);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusEditedText() throws Exception
    {
        getStatusEditedText(true);
    }

    public void testGetLocalStatusEditedText() throws Exception
    {
        getStatusEditedText(false);
    }

    private void getStatusEditedText(boolean remote) throws IOException, SCMException
    {
        File test = new File(base, "textfile1");
        FileSystemUtils.createFile(test, "hello");
        WorkingCopyStatus wcs = assertSimpleStatus("textfile1", FileStatus.State.MODIFIED, false, remote);
        assertEOL(wcs, "textfile1", FileStatus.EOLStyle.NATIVE);
    }

    public void testGetStatusEditedNewlyText() throws Exception
    {
        getStatusEditedNewlyText(true);
    }

    public void testGetLocalStatusEditedNewlyText() throws Exception
    {
        getStatusEditedNewlyText(false);
    }

    private void getStatusEditedNewlyText(boolean remote)  throws IOException, SVNException, SCMException
    {
        File test = edit("file1");
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EOL_STYLE, "native", true, false, null);

        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, false, remote);
        assertEOL(wcs, "file1", FileStatus.EOLStyle.NATIVE);
    }

    public void testGetStatusEditedLF() throws Exception
    {
        getStatusEditedLF(true);
    }

    public void testGetLocalStatusEditedLF() throws Exception
    {
        getStatusEditedLF(false);
    }

    private void getStatusEditedLF(boolean remote)  throws IOException, SCMException
    {
        File test = new File(base, "unixfile1");
        FileSystemUtils.createFile(test, "hello");
        WorkingCopyStatus wcs = assertSimpleStatus("unixfile1", FileStatus.State.MODIFIED, false, remote);
        assertEOL(wcs, "unixfile1", FileStatus.EOLStyle.LINEFEED);
    }

    public void testGetStatusEditedAddedRandomProperty() throws Exception
    {
        getStatusEditedAddRandomProperty(true);
    }

    public void testGetLocalStatusEditedAddedRandomProperty() throws Exception
    {
        getStatusEditedAddRandomProperty(false);
    }

    private void getStatusEditedAddRandomProperty(boolean remote) throws IOException, SVNException, SCMException
    {
        File test = edit("file1");
        client.doSetProperty(test, "random", "value", true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, false, remote);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusEditedAddedExecutableProperty() throws Exception
    {
        getStatusEditedAddedExecutableProperty(true);
    }

    public void testGetLocalStatusEditedAddedExecutableProperty() throws Exception
    {
        getStatusEditedAddedExecutableProperty(false);
    }

    private void getStatusEditedAddedExecutableProperty(boolean remote) throws IOException, SVNException, SCMException
    {
        File test = edit("file1");
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EXECUTABLE, "yay", true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, false, remote);
        assertExecutable(wcs, "file1", true);
    }

    public void testGetStatusEditedRemovedExecutableProperty() throws Exception
    {
        getStatusEditedRemovedExecutableProperty(true);
    }

    public void testGetLocalStatusEditedRemovedExecutableProperty() throws Exception
    {
        getStatusEditedRemovedExecutableProperty(false);
    }

    private void getStatusEditedRemovedExecutableProperty(boolean remote) throws IOException, SVNException, SCMException
    {
        File test = new File(base, "bin1");
        FileSystemUtils.createFile(test, "hello");
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EXECUTABLE, null, true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("bin1", FileStatus.State.MODIFIED, false, remote);
        assertExecutable(wcs, "bin1", false);
    }

    public void testGetStatusAdded() throws Exception
    {
        getStatusAdded(true);
    }

    public void testGetLocalStatusAdded() throws Exception
    {
        getStatusAdded(false);
    }

    private void getStatusAdded(boolean remote) throws IOException, SVNException, SCMException
    {
        File test = new File(base, "newfile");
        FileSystemUtils.createFile(test, "hello");

        client.doAdd(test, true, false, false, false);
        WorkingCopyStatus wcs = assertSimpleStatus("newfile", FileStatus.State.ADDED, false, remote);
        assertNoProperties(wcs, "newfile");
    }

    public void testGetStatusAddedText() throws Exception
    {
        getStatusAddedText(true);
    }

    public void testGetLocalStatusAddedText() throws Exception
    {
        getStatusAddedText(false);
    }

    private void getStatusAddedText(boolean remote) throws IOException, SVNException, SCMException
    {
        File test = new File(base, "newfile");
        FileSystemUtils.createFile(test, "hello");

        client.doAdd(test, true, false, false, false);
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EOL_STYLE, "native", true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("newfile", FileStatus.State.ADDED, false, remote);
        assertEOL(wcs, "newfile", FileStatus.EOLStyle.NATIVE);
    }

    public void testGetStatusDeleted() throws Exception
    {
        getStatusDeleted(true);
    }

    public void testGetLocalStatusDeleted() throws Exception
    {
        getStatusDeleted(false);
    }

    private void getStatusDeleted(boolean remote) throws SVNException, SCMException
    {
        delete("file1");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.DELETED, false, remote);
        assertNoProperties(wcs, "file1");
    }

    public void testGetStatusUnchangedOOD() throws Exception
    {
        otherEdit("file1");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.UNCHANGED, true);
        assertNoProperties(wcs, "file1");
    }

    public void testGetLocalStatusUnchangedOOD() throws Exception
    {
        otherEdit("file1");
        WorkingCopyStatus wcs = wc.getLocalStatus();
        assertEquals(0, wcs.getChanges().size());
    }

    public void testGetStatusEditedOOD() throws Exception
    {
        otherEdit("file1");
        edit("file1");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, true);
        assertNoProperties(wcs, "file1");
    }

    public void testGetLocalStatusEditedOOD() throws Exception
    {
        otherEdit("file1");
        edit("file1");
        assertSimpleStatus("file1", FileStatus.State.MODIFIED, false, false);
    }

    public void testGetStatusEditDeleted() throws Exception
    {
        otherDelete("file1");
        edit("file1");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.MODIFIED, true);
        assertNoProperties(wcs, "file1");
    }

    public void testGetLocalStatusEditDeleted() throws Exception
    {
        otherDelete("file1");
        edit("file1");
        assertSimpleStatus("file1", FileStatus.State.MODIFIED, false, false);
    }

    public void testGetStatusDeleteEdited() throws Exception
    {
        otherEdit("file1");
        delete("file1");
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.DELETED, true);
        assertNoProperties(wcs, "file1");
    }

    public void testGetLocalStatusDeleteEdited() throws Exception
    {
        otherEdit("file1");
        delete("file1");
        assertSimpleStatus("file1", FileStatus.State.DELETED, false, false);
    }

    public void testGetLocalStatusRestrictedToUnchanged() throws Exception
    {
        edit("file1");
        WorkingCopyStatus wcs = wc.getLocalStatus("dir1", "script1");
        assertEquals(0, wcs.getChanges().size());
    }

    public void testGetLocalStatusRestrictedToFiles() throws Exception
    {
        edit("bin1");
        edit("file1");
        edit("script1");
        WorkingCopyStatus wcs = wc.getLocalStatus("bin1", "script1");
        assertEquals(2, wcs.getChanges().size());
        assertEquals("bin1", wcs.getChanges().get(0).getPath());
        assertEquals("script1", wcs.getChanges().get(1).getPath());
    }

    public void testGetLocalStatusRecurses() throws Exception
    {
        edit("dir1/file1");
        edit("dir1/file2");
        WorkingCopyStatus wcs = wc.getLocalStatus("dir1");
        assertEquals(2, wcs.getChanges().size());
        assertEquals("dir1/file1", wcs.getChanges().get(0).getPath());
        assertEquals("dir1/file2", wcs.getChanges().get(1).getPath());
    }

    public void testUpdateAlreadyUpToDate() throws Exception
    {
        wc.update();
        testGetStatusNoChanges();
    }

    public void testUpdateBasic() throws Exception
    {
        otherEdit("file1");

        wc.update();
        WorkingCopyStatus wcs = wc.getStatus();
        assertEquals("5", wcs.getRevision().getRevisionString());
        assertEquals(0, wcs.getChanges().size());
    }

    public void testUpdateConflict() throws Exception
    {
        otherEdit("file1");

        File test = new File(base, "file1");
        FileSystemUtils.createFile(test, "goodbye");
        wc.update();
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.UNRESOLVED, false);
        assertEquals("5", wcs.getRevision().getRevisionString());
    }

    private File edit(String path) throws IOException
    {
        File test = new File(base, path);
        FileSystemUtils.createFile(test, "hello");
        return test;
    }

    private void delete(String path) throws SVNException
    {
        File test = new File(base, path);
        client.doDelete(test, false, false);
    }

    private void otherEdit(String path) throws SVNException, IOException
    {
        createOtherWC();
        File test = new File(otherBase, path);
        FileSystemUtils.createFile(test, "hello");
        clientManager.getCommitClient().doCommit(new File[] { test }, true, "edit file", false, false);
    }

    private void otherDelete(String path) throws SVNException
    {
        createOtherWC();
        File test = new File(otherBase, path);
        client.doDelete(test, false, false);
        clientManager.getCommitClient().doCommit(new File[] { test }, true, "delete file", false, false);
    }

    private WorkingCopyStatus assertSimpleStatus(String path, FileStatus.State state, boolean ood) throws SCMException
    {
        return assertSimpleStatus(path, state, ood, true);
    }

    private WorkingCopyStatus assertSimpleStatus(String path, FileStatus.State state, boolean ood, boolean remote) throws SCMException
    {
        WorkingCopyStatus wcs;
        if(remote)
        {
            wcs = wc.getStatus();
        }
        else
        {
            wcs = wc.getLocalStatus();
        }

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
