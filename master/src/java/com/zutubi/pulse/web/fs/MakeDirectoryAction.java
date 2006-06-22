package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.web.ActionSupport;
import com.opensymphony.util.TextUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.File;

/**
 * This action handles a create folder request.
 *
 */
public class MakeDirectoryAction extends ActionSupport
{
    /**
     * The name of the directory to be created.
     */
    private String dirName;

    /**
     * The path defining the location where the directory will be created.
     */
    private String encodedPath;

    // data required for the listing...
    private String name;
    private String type;
    private String uid;
    private String parentuid;

    public String getDirName()
    {
        return dirName;
    }

    public void setDirName(String dirName)
    {
        this.dirName = dirName;
    }

    public String getEncodedPath()
    {
        return encodedPath;
    }

    public void setEncodedPath(String encodedPath)
    {
        this.encodedPath = encodedPath;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getUid()
    {
        return uid;
    }

    public String getParentuid()
    {
        return parentuid;
    }

    public String execute()
    {
        String path = (TextUtils.stringSet(encodedPath) ? decode(encodedPath) : "");
        File f = new File(new File(""), path);
        File newFolder = new File(f, dirName);
/*
        if (!newFolder.mkdir())
        {
            return ERROR;
        }
*/

        parentuid = encodedPath;
        uid = encode(newFolder.getAbsolutePath());
        type = "folder";
        name = newFolder.getName();

        return SUCCESS;
    }

    private String decode(String encodedUid)
    {
        return new String(Base64.decodeBase64(encodedUid.getBytes()));
    }

    private String encode(String uid)
    {
        return new String(Base64.encodeBase64(uid.getBytes()));
    }
}
