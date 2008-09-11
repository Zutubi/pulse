package com.zutubi.pulse.vfs.local;

import com.zutubi.pulse.test.PulseTestCase;
import org.apache.commons.vfs.provider.local.LocalFileName;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;

import java.io.File;

/**
 *
 *
 */
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

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testResolveBasePath() throws FileSystemException
    {
        // should list the roots of the local file system.
        FileObject obj = fileSystemManager.resolveFile("local:///");
        FileObject[] roots = obj.getChildren();
        File[] expectedRoots = File.listRoots();

        assertEquals(expectedRoots.length,  roots.length);
    }
}
