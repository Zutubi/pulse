package com.zutubi.pulse.vfs.agent;

import junit.framework.TestCase;
import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.cache.DefaultFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.ram.RamFileProvider;

/**
 * <class comment/>
 */
public class RemoteFileSystemTest extends TestCase
{
    private DefaultFileSystemManager fsm;

    protected void setUp() throws Exception
    {
        super.setUp();

        fsm = new DefaultFileSystemManager();
        fsm.addProvider("file", new DefaultLocalFileProvider());
        fsm.addProvider("ram", new RamFileProvider());
        fsm.addProvider("remote", new AgentFileProvider());

        fsm.setCacheStrategy(CacheStrategy.ON_RESOLVE);
        fsm.setFilesCache(new DefaultFilesCache());

//        VFS.setUriStyle(true);
    }

    protected void tearDown() throws Exception
    {
        fsm = null;

        super.tearDown();
    }

/*
    public void testRemoteFileSystemRoots() throws FileSystemException
    {
//        FileObject fo = fsm.resolveFile("remote://host:80/c:/");
        FileObject fo = fsm.resolveFile("remote://localhost:8090/");
        print(fo.getChildren());
        print(fo.getChildren()[1].getChildren());
        print(fo.getChildren()[1].getChildren()[4].getChildren());
    }

    public void testRemoteFileInteraction() throws FileSystemException
    {
        FileObject fo = fsm.resolveFile("remote:///");
        FileObject c = fsm.resolveFile("remote://C:");
        c.getChildren();
    }

    private void print(FileObject[] files)
    {
        for (FileObject file : files)
        {
            System.out.println(file.getName().getPath());
        }
    }
*/
}
