package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.ActionResult;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

/**
 * <class comment/>
 */
public class VFSActionSupport extends ActionSupport
{
    private FileSystemManager fsManager;
    private ActionResult result = new ActionResult(this);

    protected FileSystemManager getFS() throws FileSystemException
    {
        return fsManager;
    }

    public void setFileSystemManager(FileSystemManager fsManager)
    {
        this.fsManager = fsManager;
    }

    public ActionResult getResult()
    {
        return result;
    }
}
