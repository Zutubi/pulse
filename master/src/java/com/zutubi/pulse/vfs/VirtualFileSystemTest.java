package com.zutubi.pulse.vfs;

import junit.framework.TestCase;
import org.apache.commons.vfs.impl.VirtualFileSystem;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;

import javax.mail.search.SearchException;

/**
 * <class comment/>
 */
public class VirtualFileSystemTest extends TestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

/*
    public void testVirtualMountPoints() throws FileSystemException
    {
        DefaultFileSystemManager fsm = new DefaultFileSystemManager();

        fsm.addProvider("file", new DefaultLocalFileProvider());
        fsm.init();

        // Create the virtual file system
        VirtualFileSystem vfs = (VirtualFileSystem) fsm.createVirtualFileSystem("vfs://").getFileSystem();

        vfs.addJunction("c:", fsm.resolveFile("file://c:/"));
        vfs.addJunction("c:/test", fsm.resolveFile("file://c:/tmp"));

        FileObject fo = vfs.resolveFile("/");
        fo.getChildren();
        fo.getChild("test").getChildren();
    }
*/
}
