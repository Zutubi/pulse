package com.zutubi.pulse.web.vfs;

import org.apache.commons.vfs.FileObject;

import java.io.File;

/**
 * <class comment/>
 */
public class MkdirAction extends VFSActionSupport
{
    private String root;
    private String path;
    private String name;

    public void setRoot(String root)
    {
        this.root = root;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String execute() throws Exception
    {
        FileObject fo = getFS().resolveFile(root + path);

        FileObject newFolder = fo.resolveFile(name);
        if (newFolder.exists())
        {
            // already exists.
            return ERROR;
        }

        newFolder.createFolder();
        
        return SUCCESS;
    }
}
