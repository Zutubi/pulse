package com.zutubi.pulse.master.cleanup;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DeleteFileProcessorTest extends PulseTestCase
{
    private DeleteFileProcessor processor;
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();
        tmp = FileSystemUtils.createTempDir(getName(), "test");    
        processor = new DeleteFileProcessor();
        processor.init();
    }

    public void testDeleteFile() throws IOException, ExecutionException, InterruptedException
    {
        File file = createNewFile("file.txt");

        assertTrue(processor.delete(file).get());

        assertFalse(file.exists());
    }

    public void testDeleteEmptyDirectory() throws IOException, ExecutionException, InterruptedException
    {
        File dir = createNewDir("dir");

        assertTrue(processor.delete(dir).get());

        assertFalse(dir.exists());
    }

    public void testDeleteRenameClash() throws IOException, ExecutionException, InterruptedException
    {
        File dir = createNewDir("dir");
        File file = createNewFile("dir.dead");

        assertTrue(processor.delete(dir).get());
        assertFalse(dir.exists());
        assertTrue(file.exists());
    }

    private File createNewDir(String dirname)
    {
        File d = new File(tmp, dirname);
        assertTrue(d.mkdirs());
        return d;
    }

    private File createNewFile(String filename) throws IOException
    {
        File f = new File(tmp, filename);
        assertTrue(f.createNewFile());
        return f;
    }
}
