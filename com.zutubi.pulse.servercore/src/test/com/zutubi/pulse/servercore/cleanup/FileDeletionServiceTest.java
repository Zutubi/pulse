package com.zutubi.pulse.servercore.cleanup;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class FileDeletionServiceTest extends PulseTestCase
{
    private FileDeletionService deletionService;
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir(getName(), "test");

        deletionService = new FileDeletionService();
        deletionService.init();
        deletionService.setThreadFactory(Executors.defaultThreadFactory());
    }

    protected void tearDown() throws Exception
    {
        deletionService.stop(true);

        super.tearDown();
    }

    public void testDeleteFile() throws IOException, ExecutionException, InterruptedException
    {
        File file = createNewFile("file.txt");

        assertTrue(deletionService.delete(file).get());

        assertFalse(file.exists());
    }

    public void testDeleteEmptyDirectory() throws IOException, ExecutionException, InterruptedException
    {
        File dir = createNewDir("dir");

        assertTrue(deletionService.delete(dir).get());

        assertFalse(dir.exists());
    }

    public void testDeleteRenameClash() throws IOException, ExecutionException, InterruptedException
    {
        File dir = createNewDir("dir");
        File file = createNewFile("dir.dead");

        assertTrue(deletionService.delete(dir).get());
        assertFalse(dir.exists());
        assertTrue(file.exists());
    }

    public void testRecursiveDeletion() throws IOException, ExecutionException, InterruptedException
    {
        File dir = createNewDir("dir");
        File nested = createNewDir(dir, "nested");
        File file = createNewFile(dir, "file.txt");

        assertTrue(deletionService.delete(dir).get());
        assertFalse(dir.exists());
        assertFalse(nested.exists());
        assertFalse(file.exists());
    }

    public void testNonExistantFile() throws ExecutionException, InterruptedException
    {
        assertTrue(deletionService.delete(new File("does not exist")).get());
    }

    public void testNullFile()
    {
        try
        {
            deletionService.delete(null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    private File createNewDir(String dirname)
    {
        return createNewDir(tmp, dirname);
    }

    private File createNewDir(File base, String dirname)
    {
        File d = new File(base, dirname);
        assertTrue(d.mkdirs());
        return d;
    }

    private File createNewFile(String filename) throws IOException
    {
        return createNewFile(tmp, filename);
    }

    private File createNewFile(File base, String filename) throws IOException
    {
        File f = new File(base, filename);
        assertTrue(f.createNewFile());
        return f;
    }
}
