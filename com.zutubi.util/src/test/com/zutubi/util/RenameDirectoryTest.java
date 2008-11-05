package com.zutubi.util;

import junit.framework.Assert;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.IOException;

/**
 * Test case for the rename functionality of the FileSystemUtils.
 */
public class RenameDirectoryTest extends ZutubiTestCase
{

    private File tmpDir = null;

    public RenameDirectoryTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        tmpDir = FileSystemUtils.createTempDir(RenameDirectoryTest.class.getName(), "");
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        if (!FileSystemUtils.rmdir(tmpDir))
        {
            throw new IllegalStateException("Unable to delete this tests tmp directory. Is there an open " +
                    "file handle preventing this?");
        }

        tmpDir = null;

        super.tearDown();
    }

    public void testRenameFileToNonExistingDest() throws IOException
    {
        File srcFile = new File(tmpDir, "srcFile.txt");
        junit.framework.Assert.assertTrue(srcFile.createNewFile());
        File dest = new File(tmpDir, "non-existant.txt");
        Assert.assertFalse(dest.exists());
        assertRename(srcFile, dest, false);
    }

    public void testRenameDirectoryToNonExistingDest() throws IOException
    {
        File srcDir = new File(tmpDir, "srcDir");
        Assert.assertTrue(srcDir.mkdirs());
        File dest = new File(tmpDir, "non-existant");
        Assert.assertFalse(dest.exists());
        assertRename(srcDir, dest, false);
    }

    public void testRenameToExistingDest() throws IOException
    {
        File srcDir = new File(tmpDir, "srcDir");
        Assert.assertTrue(srcDir.mkdirs());
        File dest = new File(tmpDir, "exists");
        Assert.assertTrue(dest.mkdirs());
        assertRename(srcDir, dest, true);
    }

    private void assertRename(File src, File dest, boolean force)
    {
        Assert.assertTrue(FileSystemUtils.rename(src, dest, force));
        Assert.assertTrue(dest.exists());
        Assert.assertFalse(src.exists());
    }

}