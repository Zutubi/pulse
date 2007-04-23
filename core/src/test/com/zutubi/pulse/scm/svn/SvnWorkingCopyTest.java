package com.zutubi.pulse.scm.svn;

import com.zutubi.pulse.config.PropertiesConfig;
import com.zutubi.pulse.scm.FileStatus;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.WorkingCopyStatus;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.IOUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
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
    private File branchBase;

    private SvnWorkingCopy wc;
    private SVNWCClient client;

    static
    {
        // Initialise SVN library
        SVNRepositoryFactoryImpl.setup();
    }

    //    jsankey@shiny:/tmp/wc/trunk$ ls -R
    //    .:
    //    bin1  dir2  file1  file3  macfile1  script1  textfile1  unixfile1  winfile1
    //    dir1  dir3  file2  file4  macfile2  script2  textfile2  unixfile2  winfile2
    //
    //    ./dir1:
    //    file1  file2  file3
    //
    //    ./dir2:
    //    file1  file2
    //
    //    ./dir3:
    //    file1  nested
    //
    //    ./dir3/nested:
    //    file1  file2

    protected void setUp() throws Exception
    {
        tempDir = FileSystemUtils.createTempDir(getName(), "");
        tempDir = tempDir.getCanonicalFile();

        // Create empty repo
        File repoDir = new File(tempDir, "repo");
        repoDir.mkdir();
        svnProcess = Runtime.getRuntime().exec(new String[] { "svnadmin", "create", repoDir.getAbsolutePath() });
        svnProcess.waitFor();

        // Allow anonymous writes
        File conf = new File(repoDir, FileSystemUtils.composeFilename("conf", "svnserve.conf"));
        FileSystemUtils.createFile(conf, "[general]\nanon-access = write\nauth-access = write\n");

        // Restore from dump
        File repoZip = getTestDataFile("core", "repo", "zip");
        ZipUtils.extractZip(repoZip, tempDir);

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

    private void createWC() throws ScmException, SVNException
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
        updateClient.doCheckout(SVNURL.parseURIDecoded("svn://localhost/test/trunk"), otherBase, SVNRevision.UNDEFINED, SVNRevision.HEAD, true);
    }

    private void createBranchWC() throws SVNException
    {
        branchBase = new File(tempDir, "branch");
        branchBase.mkdir();
        updateClient.doCheckout(SVNURL.parseURIDecoded("svn://localhost/test/branches/2"), branchBase, SVNRevision.UNDEFINED, SVNRevision.HEAD, true);
    }

    protected void tearDown() throws Exception
    {
        svnProcess.destroy();
        svnProcess.waitFor();
        svnProcess = null;
        Thread.sleep(100);

        FileSystemUtils.rmdir(tempDir);

        updateClient = null;
        client = null;
        wc = null;
    }

    public void testMatchesRepositoryMatches() throws ScmException
    {
        Properties p = new Properties();
        p.put(SvnConstants.PROPERTY_URL, "svn://localhost/test/trunk");
        assertTrue(wc.matchesRepository(p));
    }

    public void testMatchesRepositoryDoesntMatch() throws ScmException
    {
        Properties p = new Properties();
        p.put(SvnConstants.PROPERTY_URL, "svn://localhost/test/branches/1.0.x");
        assertFalse(wc.matchesRepository(p));
    }

    public void testMatchesRepositoryEmbeddedUser() throws ScmException
    {
        Properties p = new Properties();
        p.put(SvnConstants.PROPERTY_URL, "svn://goober@localhost/test/trunk");
        assertTrue(wc.matchesRepository(p));
    }

    public void testGetStatusNoChanges() throws Exception
    {
        WorkingCopyStatus wcs = wc.getStatus();
        assertEquals("6", wcs.getRevision().getRevisionString());
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

    private void getStatusEdited(boolean remote) throws Exception
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

    private void getStatusEditedText(boolean remote) throws IOException, ScmException
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

    private void getStatusEditedNewlyText(boolean remote)  throws IOException, SVNException, ScmException
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

    private void getStatusEditedLF(boolean remote)  throws IOException, ScmException
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

    private void getStatusEditedAddRandomProperty(boolean remote) throws IOException, SVNException, ScmException
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

    private void getStatusEditedAddedExecutableProperty(boolean remote) throws IOException, SVNException, ScmException
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

    private void getStatusEditedRemovedExecutableProperty(boolean remote) throws IOException, SVNException, ScmException
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

    private void getStatusAdded(boolean remote) throws IOException, SVNException, ScmException
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

    private void getStatusAddedText(boolean remote) throws IOException, SVNException, ScmException
    {
        File test = new File(base, "newfile");
        FileSystemUtils.createFile(test, "hello");

        client.doAdd(test, true, false, false, false);
        client.doSetProperty(test, SvnConstants.SVN_PROPERTY_EOL_STYLE, "native", true, false, null);
        WorkingCopyStatus wcs = assertSimpleStatus("newfile", FileStatus.State.ADDED, false, remote);
        assertEOL(wcs, "newfile", FileStatus.EOLStyle.NATIVE);
    }

    public void testGetLocalStatusAddedDirectory() throws Exception
    {
        File test = new File(base, "newdir");
        assertTrue(test.mkdir());

        client.doAdd(test, true, false, false, false);
        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(1, status.getChanges().size());
        assertAdded(status, "newdir", true);
    }

    public void testGetLocalStatusAddedDirectoryChildFile() throws Exception
    {
        File dir = new File(base, "newdir");
        assertTrue(dir.mkdir());

        File child = new File(dir, "newfile");
        FileSystemUtils.createFile(child, "test");

        client.doAdd(dir, true, false, false, true);
        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(2, status.getChanges().size());
        assertAdded(status, "newdir", true);
        assertAdded(status, "newdir/newfile", false);
    }

    public void testGetLocalStatusAddedDirectoryChildFileNotAdded() throws Exception
    {
        File dir = new File(base, "newdir");
        assertTrue(dir.mkdir());

        File child = new File(dir, "newfile");
        FileSystemUtils.createFile(child, "test");

        client.doAdd(dir, true, false, false, false);
        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(1, status.getChanges().size());
        assertAdded(status, "newdir", true);
    }

    public void testGetLocalStatusMoved() throws Exception
    {
        move("file1", "movedfile1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(2, status.getChanges().size());
        assertDeleted(status, "file1", false);
        assertAdded(status, "movedfile1", false);
    }

    public void testGetLocalStatusMovedDir() throws Exception
    {
        move("dir1", "moveddir1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(8, status.getChanges().size());
        assertDeleted(status, "dir1", true);
        assertDeleted(status, "dir1/file1", false);
        assertDeleted(status, "dir1/file2", false);
        assertDeleted(status, "dir1/file3", false);
        assertAdded(status, "moveddir1", true);
        assertAdded(status, "moveddir1/file1", false);
        assertAdded(status, "moveddir1/file2", false);
        assertAdded(status, "moveddir1/file3", false);
    }

    public void testGetLocalStatusMovedDirNestedChild() throws Exception
    {
        move("dir3", "moveddir3");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(10, status.getChanges().size());
        assertDeleted(status, "dir3", true);
        assertDeleted(status, "dir3/file1", false);
        assertDeleted(status, "dir3/nested", true);
        assertDeleted(status, "dir3/nested/file1", false);
        assertDeleted(status, "dir3/nested/file2", false);
        assertAdded(status, "moveddir3", true);
        assertAdded(status, "moveddir3/file1", false);
        assertAdded(status, "moveddir3/nested", true);
        assertAdded(status, "moveddir3/nested/file1", false);
        assertAdded(status, "moveddir3/nested/file2", false);
    }

    public void testGetLocalStatusMovedDirModifiedChild() throws Exception
    {
        move("dir2", "moveddir2");
        edit("moveddir2/file1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(6, status.getChanges().size());
        assertDeleted(status, "dir2", true);
        assertDeleted(status, "dir2/file1", false);
        assertDeleted(status, "dir2/file2", false);
        assertAdded(status, "moveddir2", true);
        assertModified(status, "moveddir2/file1", false);
        assertAdded(status, "moveddir2/file2", false);
    }

    public void testGetLocalStatusMovedDirDeletedChild() throws Exception
    {
        move("dir2", "moveddir2");
        delete("moveddir2/file1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(6, status.getChanges().size());
        assertDeleted(status, "dir2", true);
        assertDeleted(status, "dir2/file1", false);
        assertDeleted(status, "dir2/file2", false);
        assertAdded(status, "moveddir2", true);
        assertDeleted(status, "moveddir2/file1", false);
        assertAdded(status, "moveddir2/file2", false);
    }

    public void getLocalStatusMergeEdited() throws Exception
    {
        long rev = branchEdit("file1");
        doMerge(rev);
        assertSimpleStatus("file1", FileStatus.State.MODIFIED, false);
    }

    public void getLocalStatusMergeAdded() throws Exception
    {
        long rev = branchAdd("newfile");
        doMerge(rev);
        assertSimpleStatus("newfile", FileStatus.State.ADDED, false);
    }

    public void getLocalStatusMergeDeleted() throws Exception
    {
        long rev = branchDelete("file1");
        doMerge(rev);
        assertSimpleStatus("file1", FileStatus.State.DELETED, false);
    }

    public void getLocalStatusMergeMoved() throws Exception
    {
        long rev = branchMove("file1", "movedfile1");
        doMerge(rev);
        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(2, status.getChanges().size());
        assertDeleted(status, "file1", false);
        assertAdded(status, "movedfile1", false);
    }

    public void getLocalStatusMergeMovedDir() throws Exception
    {
        long rev = branchMove("dir2", "moveddir2");
        doMerge(rev);
        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(6, status.getChanges().size());
        assertDeleted(status, "dir2", true);
        assertDeleted(status, "dir2/file1", false);
        assertDeleted(status, "dir2/file2", false);
        assertAdded(status, "moveddir2", true);
        assertAdded(status, "moveddir2/file1", false);
        assertAdded(status, "moveddir2/file2", false);
    }

    public void getLocalStatusMergeConflict() throws Exception
    {
        edit("file1");
        long rev = branchEdit("file1");
        doMerge(rev);
        assertSimpleStatus("file1", FileStatus.State.UNRESOLVED, false);
    }

    public void getLocalStatusMergeEditDeleted() throws Exception
    {
        edit("file1");
        long rev = branchDelete("file1");
        doMerge(rev);
        // Forced merge deletes locally edited file
        assertSimpleStatus("file1", FileStatus.State.DELETED, false);
    }

    public void testGetStatusDeleted() throws Exception
    {
        getStatusDeleted(true);
    }

    public void testGetLocalStatusDeleted() throws Exception
    {
        getStatusDeleted(false);
    }

    private void getStatusDeleted(boolean remote) throws SVNException, ScmException
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
        assertEquals("7", wcs.getRevision().getRevisionString());
        assertEquals(0, wcs.getChanges().size());
    }

    public void testUpdateConflict() throws Exception
    {
        otherEdit("file1");

        File test = new File(base, "file1");
        FileSystemUtils.createFile(test, "goodbye");
        wc.update();
        WorkingCopyStatus wcs = assertSimpleStatus("file1", FileStatus.State.UNRESOLVED, false);
        assertEquals("7", wcs.getRevision().getRevisionString());
    }

    private File edit(String path) throws IOException, SVNException
    {
        doEdit(path, base, false);
        return new File(base, path);
    }

    private void otherEdit(String path) throws SVNException, IOException
    {
        createOtherWC();
        doEdit(path, otherBase, true);
    }

    private long branchEdit(String path) throws SVNException, IOException
    {
        createBranchWC();
        return doEdit(path, branchBase, true);
    }

    private long doEdit(String path, File baseDir, boolean commit) throws IOException, SVNException
    {
        File test = new File(baseDir, path);
        FileSystemUtils.createFile(test, test.getAbsolutePath());
        if(commit)
        {
            SVNCommitInfo info = clientManager.getCommitClient().doCommit(new File[] { test }, true, "edit file", false, false);
            return info.getNewRevision();
        }
        return -1;
    }

    private long branchAdd(String path) throws SVNException, IOException
    {
        createBranchWC();
        return doAdd(path, branchBase, true);
    }

    private long doAdd(String path, File baseDir, boolean commit) throws IOException, SVNException
    {
        File test = new File(baseDir, path);
        FileSystemUtils.createFile(test, test.getAbsolutePath());
        clientManager.getWCClient().doAdd(test, true, false, false, true);

        if(commit)
        {
            SVNCommitInfo info = clientManager.getCommitClient().doCommit(new File[] { test }, true, "add file", false, false);
            return info.getNewRevision();
        }
        return -1;
    }

    private void delete(String path) throws SVNException
    {
        doDelete(path, base, false);
    }

    private void otherDelete(String path) throws SVNException
    {
        createOtherWC();
        doDelete(path, otherBase, true);
    }

    private long branchDelete(String path) throws SVNException
    {
        createBranchWC();
        return doDelete(path, branchBase, true);
    }

    private long doDelete(String path, File baseDir, boolean commit) throws SVNException
    {
        File test = new File(baseDir, path);
        client.doDelete(test, false, false);
        if(commit)
        {
            SVNCommitInfo info = clientManager.getCommitClient().doCommit(new File[] { test }, true, "delete file", false, false);
            return info.getNewRevision();
        }
        return -1;
    }

    private long move(String srcPath, String destPath) throws SVNException, IOException
    {
        return doMove(srcPath, destPath, base, false);
    }

    private long branchMove(String srcPath, String destPath) throws SVNException, IOException
    {
        createBranchWC();
        return doMove(srcPath, destPath, branchBase, true);
    }

    private long doMove(String srcPath, String destPath, File baseDir, boolean commit) throws IOException, SVNException
    {
        clientManager.getMoveClient().doMove(new File(baseDir, srcPath), new File(baseDir, destPath));

        if(commit)
        {
            SVNCommitInfo info = clientManager.getCommitClient().doCommit(new File[] { baseDir }, true, "move", false, true);
            return info.getNewRevision();
        }
        return -1;
    }

    private void doMerge(long change) throws SVNException
    {
        SVNURL branchUrl = SVNURL.parseURIDecoded("svn://localhost/test/branches/2");
        clientManager.getDiffClient().doMerge(branchUrl, SVNRevision.create(change - 1), branchUrl, SVNRevision.create(change), base, true, false, true, false);
    }

    private WorkingCopyStatus assertSimpleStatus(String path, FileStatus.State state, boolean ood) throws ScmException
    {
        return assertSimpleStatus(path, state, ood, true);
    }

    private WorkingCopyStatus assertSimpleStatus(String path, FileStatus.State state, boolean ood, boolean remote) throws ScmException
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

    private void assertModified(WorkingCopyStatus status, String path, boolean dir)
    {
        assertFileState(status, path, dir, FileStatus.State.MODIFIED);
    }

    private void assertAdded(WorkingCopyStatus status, String path, boolean dir)
    {
        assertFileState(status, path, dir, FileStatus.State.ADDED);
    }

    private void assertDeleted(WorkingCopyStatus status, String path, boolean dir)
    {
        assertFileState(status, path, dir, FileStatus.State.DELETED);
    }

    private void assertFileState(WorkingCopyStatus status, String path, boolean dir, FileStatus.State fileState)
    {
        FileStatus fs = status.getFileStatus(path);
        assertNotNull(fs);
        assertEquals(dir, fs.isDirectory());
        assertEquals(fileState, fs.getState());
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
