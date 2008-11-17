package com.zutubi.pulse.master.vfs.provider.local;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.SystemUtils;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

import java.io.File;

public class LocalFileSystemTest extends PulseTestCase
{
    private DefaultFileSystemManager fileSystemManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        fileSystemManager = new DefaultFileSystemManager();
        fileSystemManager.addProvider("local", new DefaultLocalFileProvider());
        fileSystemManager.init();
    }

    public void testResolveRootPathOnWindows() throws FileSystemException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        FileObject obj = fileSystemManager.resolveFile("local:/");
        assertTrue(obj.exists());
        assertTrue(obj.isReadable());
    }

    public void testRootPathListingOnWindows() throws FileSystemException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        FileObject obj = fileSystemManager.resolveFile("local:/");
        FileObject[] listing = obj.getChildren();
        File[] expectedRoots = File.listRoots();

        assertEquals(expectedRoots.length, listing.length);
    }

    public void testResolveCDriveOnWindows() throws FileSystemException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        FileObject obj = fileSystemManager.resolveFile("local:/C:");
        assertTrue(obj.exists());
    }

    public void testCDriveListingOnWindows() throws FileSystemException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        FileObject obj = fileSystemManager.resolveFile("local:/C:");
        FileObject[] listing = obj.getChildren();

        File cdrive = new File("C:/");
        File[] expectedListing = cdrive.listFiles();

        assertEquals(expectedListing.length, listing.length);
        for (int i = 0; i < expectedListing.length; i++)
        {
            assertEquals(expectedListing[i].getName(), listing[i].getName().getBaseName());
        }
    }
}
