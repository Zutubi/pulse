package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.web.ActionSupport;

import java.io.File;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;

/**
 * <class-comment/>
 */
public class MkdirAction extends ActionSupport
{
    /**
     * The path identifier specifying the working directory for this command.
     */
    private String pid;

    /**
     * The name of the new directory to be created.
     */
    private String name;

    public void setPid(String pid)
    {
        this.pid = pid;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String execute() throws Exception
    {
        // convert path to location on file system.
        File path = decodePath(pid);
        if (path == null)
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

    protected File decodePath(String pid)
    {
        File file = null;
        StringTokenizer tokens = new StringTokenizer(pid, "/", false);
        while (tokens.hasMoreTokens())
        {
            String t = tokens.nextToken();
            if (file == null)
            {
                file = new File(decode(t));
            }
            else
            {
                file = new File(file, decode(t));
            }
        }
        return file;
    }

    private String decode(String encodedUid)
    {
        return new String(Base64.decodeBase64(encodedUid.getBytes()));
    }
}
