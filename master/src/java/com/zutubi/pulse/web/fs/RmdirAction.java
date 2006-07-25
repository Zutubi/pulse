package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.web.ActionSupport;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.util.StringTokenizer;

/**
 * <class-comment/>
 */
public class RmdirAction extends ActionSupport
{
    /**
     * The path identifier specifying the working directory for this command.
     */
    private String pid;

    /**
     *
     */
    public void setPid(String pid)
    {
        this.pid = pid;
    }

    public String execute() throws Exception
    {
        File f = new File(pid);

        // check whether or not the directory f is empty.
        if (!f.isDirectory())
        {
            return ERROR;
        }

        if (f.list().length > 0)
        {
            return ERROR;
        }

        // delete the directory, all is well.
        if (!f.delete())
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
