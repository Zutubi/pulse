/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core.util;

import java.io.File;
import java.io.IOException;

import com.zutubi.pulse.test.PulseTestCase;

/**
 * Test case for the rename functionality of the FileSystemUtils.
 */
public class RenameDirectoryTest extends PulseTestCase
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
        tmpDir = FileSystemUtils.createTempDirectory(ZipTest.class.getName(), "");
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        if (!FileSystemUtils.removeDirectory(tmpDir))
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
        assertTrue(srcFile.createNewFile());
        File dest = new File(tmpDir, "non-existant.txt");
        assertFalse(dest.exists());
        assertRename(srcFile, dest, false);
    }

    public void testRenameDirectoryToNonExistingDest() throws IOException
    {
        File srcDir = new File(tmpDir, "srcDir");
        assertTrue(srcDir.mkdirs());
        File dest = new File(tmpDir, "non-existant");
        assertFalse(dest.exists());
        assertRename(srcDir, dest, false);
    }

    public void testRenameToExistingDest() throws IOException
    {
        File srcDir = new File(tmpDir, "srcDir");
        assertTrue(srcDir.mkdirs());
        File dest = new File(tmpDir, "exists");
        assertTrue(dest.mkdirs());
        assertRename(srcDir, dest, true);
    }

    private void assertRename(File src, File dest, boolean force)
    {
        assertTrue(FileSystemUtils.rename(src, dest, force));
        assertTrue(dest.exists());
        assertFalse(src.exists());
    }

}