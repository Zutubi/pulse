package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.web.ActionSupport;

import java.io.File;

/**
 * <class-comment/>
 */
public class MkdirAction extends ActionSupport
{
    /**
     * The path identifier specifying the working directory for this command.
     */
    private String path;

    /**
     * The name of the new directory to be created.
     */
    private String name;

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
        // convert path to location on file system.
        File path = new File(this.path);
        if (!path.isDirectory())
        {
            return ERROR;
        }

        // attempt to create the directory with specified name.
        File newFolder = new File(path, name);

        // return success / error response.
        if (!newFolder.mkdirs())
        {
            return ERROR;
        }

        return SUCCESS;
    }
}
