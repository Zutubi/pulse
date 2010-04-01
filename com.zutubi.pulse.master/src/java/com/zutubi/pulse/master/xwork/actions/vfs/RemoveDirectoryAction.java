package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.util.StringUtils;
import com.zutubi.tove.type.record.PathUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.UriParser;

/**
 *  An action to remove a directory from the local file system
 */
public class RemoveDirectoryAction extends VFSActionSupport
{
    private String basePath;
    private String path;

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String execute() throws Exception
    {
        String fullPath = "local://";
        if (StringUtils.stringSet(basePath))
        {
            fullPath += "/" + UriParser.encode(PathUtils.normalisePath(basePath));
        }
        if(StringUtils.stringSet(path))
        {
            fullPath += "/" + UriParser.encode(PathUtils.normalisePath(path));
        }

        final FileObject fo = getFS().resolveFile(fullPath);

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
