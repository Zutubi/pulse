/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class P4ServerTest extends PulseTestCase
{
    P4Server server;
    private File tmpDir;
    private boolean generateMode = false;

    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDirectory(getClass().getName(), "");
    }

    protected void tearDown() throws Exception
    {
        server = null;
        FileSystemUtils.removeDirectory(tmpDir);
        super.tearDown();
    }

    public void testGetLocation()
    {
        getServer("test-client");
        assertEquals(server.getLocation(), "test-client@:1666");
    }

    public void testGetLatestRevision() throws SCMException
    {
        getServer("test-client");
        assertEquals(8, server.getLatestRevision().getRevisionNumber());
    }

    public void testCheckoutHead() throws Exception
    {
        getServer("depot-client");
        List<Change> changes = new LinkedList<Change>();
        NumericalRevision revision = (NumericalRevision) server.checkout(randomInt(), tmpDir, null, changes);
        assertEquals(8, revision.getRevisionNumber());

        assertEquals(10, changes.size());
        for (int i = 0; i < 10; i++)
        {
            Change change = changes.get(i);
            assertEquals(Change.Action.ADD, change.getAction());

            // Foolish of me to create file10 which ruins lexical ordering :|
            int number;
            if (i == 0)
            {
                number = 1;
            }
            else if (i == 1)
            {
                number = 10;
            }
            else
            {
                number = i;
            }

            assertEquals(String.format("//depot/file%d", number), change.getFilename());
        }

        checkDirectory("checkoutHead");
    }

    public void testCheckoutRevision() throws Exception
    {
        getServer("depot-client");
        NumericalRevision revision = (NumericalRevision) server.checkout(randomInt(), tmpDir, new NumericalRevision(1), null);
        assertEquals(1, revision.getRevisionNumber());
        checkDirectory("checkoutRevision");
    }

    public void testCheckoutFile() throws SCMException
    {
        getServer("depot-client");
        String content = server.checkout(1, null, FileSystemUtils.composeFilename("depot", "file2"));
        assertEquals("content of file2: edited at the same time as file2 in depot2.\n", content);
    }

    public void testCheckoutFileRevision() throws SCMException
    {
        getServer("depot-client");
        String content = server.checkout(1, new NumericalRevision(2), FileSystemUtils.composeFilename("depot", "file2"));
        assertEquals("content of file2\n", content);
    }

    public void testHasChangedSince() throws Exception
    {
        getServer("test-client");
        assertTrue(server.hasChangedSince(new NumericalRevision(6)));
    }

    public void testHasChangedSinceRestrictedToView() throws Exception
    {
        getServer("depot-client");
        assertFalse(server.hasChangedSince(new NumericalRevision(6)));
    }

    public void testGetChanges() throws Exception
    {
        getServer("test-client");
        List<Changelist> changes = server.getChanges(new NumericalRevision(1), new NumericalRevision(7), "");
        assertEquals(6, changes.size());
        Changelist list = changes.get(4);
        assertEquals("Delete and edit files in depot2.", list.getComment());
        assertEquals("test-user", list.getUser());
        assertEquals(3, ((NumericalRevision) list.getRevision()).getRevisionNumber());
        List<Change> changedFiles = list.getChanges();
        assertEquals(2, changedFiles.size());
        Change file1 = changedFiles.get(0);
        assertEquals("//depot2/file1", file1.getFilename());
        assertEquals(Change.Action.EDIT, file1.getAction());
        Change file10 = changedFiles.get(1);
        assertEquals("//depot2/file10", file10.getFilename());
        assertEquals(Change.Action.DELETE, file10.getAction());
    }

    public void testGetChangesRestrictedToView() throws Exception
    {
        getServer("depot-client");
        List<Changelist> changes = server.getChanges(new NumericalRevision(1), new NumericalRevision(7), "");
        assertEquals(1, changes.size());
        assertEquals(4, ((NumericalRevision) changes.get(0).getRevision()).getRevisionNumber());
    }

    public void testListNonExistent() throws SCMException
    {
        getServer("test-client");
        try
        {
            server.getListing("depot4");
            fail();
        }
        catch (SCMException e)
        {
            assertTrue(e.getMessage().contains("does not exist"));
        }
    }

    public void testListRoot() throws SCMException
    {
        getServer("test-client");
        List<RemoteFile> files = server.getListing("");
        assertEquals(2, files.size());
        RemoteFile f = files.get(0);
        assertEquals("depot", f.getName());
        assertTrue(f.isDirectory());

        f = files.get(1);
        assertEquals("depot2", f.getName());
        assertEquals("depot2", f.getPath());
        assertTrue(f.isDirectory());
    }

    public void testListPath() throws SCMException
    {
        getServer("test-client");
        List<RemoteFile> files = server.getListing("depot2");
        assertEquals(10, files.size());

        RemoteFile f;
        for (int i = 0; i < 9; i++)
        {
            f = files.get(i);
            assertTrue(f.isFile());
            assertEquals("file" + (i + 1), f.getName());
            assertEquals("depot2/file" + (i + 1), f.getPath());
            assertEquals("text/plain", f.getMimeType());
        }

        f = files.get(9);
        assertEquals("test-branch", f.getName());
        assertEquals("depot2/test-branch", f.getPath());
        assertTrue(f.isDirectory());
    }

    public void testListComplexClient() throws SCMException
    {
        getServer("complex-client");
        List<RemoteFile> files = server.getListing("");
        assertEquals(1, files.size());
        RemoteFile remoteFile = files.get(0);
        assertEquals("src", remoteFile.getName());
        assertTrue(remoteFile.isDirectory());
    }

    public void testListComplexSrc() throws SCMException
    {
        getServer("complex-client");
        List<RemoteFile> files = server.getListing("src");
        assertEquals(2, files.size());

        RemoteFile remoteFile = files.get(0);
        assertEquals("host", remoteFile.getName());
        assertTrue(remoteFile.isDirectory());

        remoteFile = files.get(1);
        assertEquals("libraries", remoteFile.getName());
        assertTrue(remoteFile.isDirectory());
    }

    public void testListComplexSnuth() throws SCMException
    {
        getServer("complex-client");
        List<RemoteFile> files = server.getListing("src/libraries/snuth");
        assertEquals(2, files.size());

        RemoteFile remoteFile = files.get(0);
        assertEquals("Makefile", remoteFile.getName());
        assertFalse(remoteFile.isDirectory());

        remoteFile = files.get(1);
        assertEquals("source.c", remoteFile.getName());
        assertFalse(remoteFile.isDirectory());
    }

    private void getServer(String client)
    {
        server = new P4Server(":1666", "test-user", "", client);
    }

    private int randomInt()
    {
        double random = Math.random() * 100;
        return (int) random;
    }

    private void checkDirectory(String name) throws IOException
    {
        File expectedRoot = getDataRoot();
        File expectedDir = new File(expectedRoot, name);

        if (generateMode)
        {
            tmpDir.renameTo(expectedDir);
        }
        else
        {
            assertDirectoriesEqual(expectedDir, tmpDir);
        }
    }

    private File getDataRoot()
    {
        return new File(getPulseRoot(), FileSystemUtils.composeFilename("server-core", "src", "test", "com", "zutubi", "pulse", "scm", "p4", "data"));
    }
}
