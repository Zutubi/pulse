package com.zutubi.pulse.util;

import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.io.IOException;

/**
 * <class comment/>
 */
public class CopyUtilsTest extends PulseTestCase
{
    private File base = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        base = FileSystemUtils.createTempDirectory();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(base);

        super.tearDown();
    }

    public void testCopyFileToNonExistantFile() throws IOException
    {
        File src = new File(base, "src.txt");
        FileSystemUtils.createFile(src, "Some text.");

        File dest = new File(base, "dest.txt");

        assertFalse(dest.isFile());
        CopyUtils.copy(dest, src);
        assertTrue(dest.isFile());
    }

    public void testCopyFileToDirectory() throws IOException
    {
        File src = new File(base, "src.txt");
        FileSystemUtils.createFile(src, "Some text.");

        File dest = new File(base, "dest");
        assertTrue(dest.mkdirs());

        assertTrue(dest.isDirectory());
        CopyUtils.copy(dest, src);
        assertTrue(new File(dest, "src.txt").isFile());
    }

    public void testCopyMultipleFiles() throws IOException
    {
        File srcA = new File(base, "srca.txt");
        FileSystemUtils.createFile(srcA, "Some text.");
        File srcB = new File(base, "srcb.txt");
        FileSystemUtils.createFile(srcB, "Some text.");

        File dest = new File(base, "dest");

        assertFalse(dest.isDirectory());
        CopyUtils.copy(dest, srcA, srcB);
        
        assertTrue(dest.isDirectory());
        assertTrue(new File(dest, "srca.txt").isFile());
        assertTrue(new File(dest, "srcb.txt").isFile());
    }

    public void testCopyDirectoryToNonExistantFile() throws IOException
    {
        File dir = new File(base, "dir");
        dir.mkdirs();

        FileSystemUtils.createFile(new File(dir, "a.txt"), "Text file a");
        FileSystemUtils.createFile(new File(dir, "b.txt"), "Text file a");

        File dest = new File(base, "dest");
        assertFalse(dest.exists());
        CopyUtils.copy(dest, dir);
        assertTrue(dest.isDirectory());
        assertTrue(new File(dest, "a.txt").isFile());
        assertTrue(new File(dest, "b.txt").isFile());
    }
}
