package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.util.StringUtils;
import com.zutubi.tove.type.record.PathUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.provider.UriParser;

/**
 * An action to make a new directory in the local file system
 */
public class MakeDirectoryAction extends VFSActionSupport
{
    private String basePath;
    private String path;
    private String name;

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setName(String name)
    {
        this.name = name;
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

        FileObject fo = getFS().resolveFile(fullPath);

        FileObject newFolder = fo.resolveFile(name);
        if (newFolder.exists())
        {
            addActionError(String.format("The folder '%s' already exists.", name));
            return ERROR;
        }

        try
        {
            newFolder.createFolder();
            return SUCCESS;
        }
        catch (Exception e)
        {
            addActionError(e.getMessage());
            return ERROR;
        }
    }
}
