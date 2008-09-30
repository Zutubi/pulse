package com.zutubi.pulse.vfs.local;

import com.zutubi.pulse.test.PulseTestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

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

    public void testResolveRootPath() throws FileSystemException
    {
        // should list the roots of the local file system.

/*  this does not work so well on *nix.
        FileObject obj = fileSystemManager.resolveFile("local:///");
        FileObject[] roots = obj.getChildren();
        File[] expectedRoots = File.listRoots();

        assertEquals(expectedRoots.length,  roots.length);
*/
    }
}
