package com.zutubi.pulse.web.vfs;

import com.zutubi.pulse.web.ActionSupport;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

/**
 * <class comment/>
 */
public class VFSActionSupport extends ActionSupport
{
    private FileSystemManager fsManager;

    protected FileSystemManager getFS() throws FileSystemException
    {
        return fsManager;
    }

    public void setFileSystemManager(FileSystemManager fsManager)
    {
        this.fsManager = fsManager;
    }
}
