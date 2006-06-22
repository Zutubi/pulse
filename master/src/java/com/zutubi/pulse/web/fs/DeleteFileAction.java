package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.web.ActionSupport;
import org.apache.commons.codec.binary.Base64;

import java.io.File;

/**
 *
 *
 */
public class DeleteFileAction extends ActionSupport
{
    private String encodedPath;

    public String getEncodedPath()
    {
        return encodedPath;
    }

    public void setEncodedPath(String encodedPath)
    {
        this.encodedPath = encodedPath;
    }

    public String execute() throws Exception
    {
        String path = decode(encodedPath);

        File f = new File(new File(""), path);
        System.out.println("DELETE: " + f);
/*
        if (!f.delete())
        {
            // failed.
        }
*/
        return SUCCESS;
    }

    private String decode(String encodedUid)
    {
        return new String(Base64.decodeBase64(encodedUid.getBytes()));
    }
}
