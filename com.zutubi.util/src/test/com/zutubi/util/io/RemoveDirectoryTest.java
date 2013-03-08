package com.zutubi.util.io;

import com.google.common.io.Files;
import com.zutubi.util.junit.ZutubiTestCase;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 */
public class RemoveDirectoryTest extends ZutubiTestCase
{
    public void testRemoveNonExistant() throws IOException
    {
        FileSystemUtils.rmdir(new File("/this/directory/does/not/exist"));
    }

    public void testRemoveEmpty() throws IOException
    {
        File tmpDir = createTmpDir();
        FileSystemUtils.rmdir(tmpDir);
        Assert.assertFalse(tmpDir.exists());
    }

    public void testRemoveContents() throws IOException
    {
        File tmpDir = createTmpDir();
        File aFile = new File(tmpDir, "aFile");
        File anotherFile = new File(tmpDir, "anotherFile");

        Files.write("content", aFile, Charset.defaultCharset());
        Files.write("content", anotherFile, Charset.defaultCharset());

        FileSystemUtils.rmdir(tmpDir);
        Assert.assertFalse(tmpDir.exists());
        Assert.assertFalse(aFile.exists());
        Assert.assertFalse(anotherFile.exists());
    }

    public void testRemoveNested() throws IOException
    {
        File tmpDir = createTmpDir();

        File aFile = new File(tmpDir, "aFile");
        Files.write("content", aFile, Charset.defaultCharset());

        File nestedDir = new File(tmpDir, "nested");
        Assert.assertTrue(nestedDir.mkdirs());

        File nestedFile = new File(nestedDir, "aFile");
        Files.write("data", nestedFile, Charset.defaultCharset());

        FileSystemUtils.rmdir(tmpDir);
        Assert.assertFalse(tmpDir.exists());
        Assert.assertFalse(nestedDir.exists());
    }

    public void testExternalSymlink() throws IOException, InterruptedException
    {
        File tmpDir = createTmpDir();
        File symlink = new File(tmpDir, "symlink");
        File linkDestination = createTmpDir();

        if (FileSystemUtils.createSymlink(symlink, linkDestination))
        {
            FileSystemUtils.rmdir(tmpDir);
            Assert.assertTrue(linkDestination.isDirectory());
        }
        else
        {
            FileSystemUtils.rmdir(tmpDir);
        }

        FileSystemUtils.rmdir(linkDestination);
    }

    private File createTmpDir() throws IOException
    {
        return FileSystemUtils.createTempDir(RemoveDirectoryTest.class.getName(), "");
    }
}
