package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.util.StringUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;

/**
 * <class comment/>
 */
public class RmAction extends VFSActionSupport
{
    /**
     * @deprecated
     */
    private String root;

    private String path;

    /**
     * @deprecated
     */
    public void setRoot(String root)
    {
        this.root = root;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws Exception
    {
        if (StringUtils.stringSet(root))
        {
            path = root + path;
        }

        FileObject fo = getFS().resolveFile(path);

        // we only support deleting empty directories for safety.
        FileType type = fo.getType();
        if (type != FileType.FOLDER)
        {
            addActionError("Removing files is not supported.");
            return ERROR;
        }

        FileObject[] children = fo.getChildren();
        if (children != null && children.length > 0)
        {
            addActionError("You can not delete this directory. It is not empty.");
            return ERROR;
        }

        if (!fo.delete())
        {
            addActionError("Deleting folder failed. The reason is unknown.");
            return ERROR;
        }

        return SUCCESS;
    }
}
