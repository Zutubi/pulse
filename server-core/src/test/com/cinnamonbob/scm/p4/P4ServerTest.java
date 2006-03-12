package com.cinnamonbob.scm.p4;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.NumericalRevision;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.test.BobTestCase;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class P4ServerTest extends BobTestCase
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
        assertEquals(7, server.getLatestRevision().getRevisionNumber());
    }

    public void testCheckoutHead() throws Exception
    {
        getServer("depot-client");
        List<Change> changes = new LinkedList<Change>();
        NumericalRevision revision = (NumericalRevision) server.checkout(randomInt(), tmpDir, null, changes);
        assertEquals(7, revision.getRevisionNumber());

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
        String content = server.checkout(null, FileSystemUtils.composeFilename("depot", "file2"));
        assertEquals("content of file2: edited at the same time as file2 in depot2.\n", content);
    }

    public void testCheckoutFileRevision() throws SCMException
    {
        getServer("depot-client");
        String content = server.checkout(new NumericalRevision(2), FileSystemUtils.composeFilename("depot", "file2"));
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
        return new File(getBobRoot(), FileSystemUtils.composeFilename("server-core", "src", "test", "com", "cinnamonbob", "scm", "p4", "data"));
    }
}
