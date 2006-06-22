package com.zutubi.pulse.web.tree;

import com.zutubi.pulse.filesystem.File;
import com.zutubi.pulse.filesystem.FileSystem;
import com.zutubi.pulse.filesystem.FileSystemException;
import com.zutubi.pulse.filesystem.local.LocalFileSystem;
import com.zutubi.pulse.web.ActionSupport;
import com.opensymphony.util.TextUtils;
import org.apache.commons.codec.binary.Base64;

import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class PopupFormAction extends ActionSupport
{
    private String formname;
    private String fieldname;
    private String initPath;
    private LinkedList<String> uids;

    public String getFormname()
    {
        return formname;
    }

    public void setFormname(String formname)
    {
        this.formname = formname;
    }

    public String getFieldname()
    {
        return fieldname;
    }

    public void setFieldname(String fieldname)
    {
        this.fieldname = fieldname;
    }

    public void setInitPath(String path)
    {
        this.initPath = path;
    }

    public LinkedList<String> getUids()
    {
        return uids;
    }

    public String execute() throws FileSystemException
    {
        uids = new LinkedList<String>();

        if (TextUtils.stringSet(initPath))
        {
            FileSystem fileSystem = getFileSystem();
            File f = fileSystem.getFile(initPath);
            while (f != null)
            {
                uids.addFirst(encode(f.getPath()));
                f = f.getParentFile();
            }
        }
        uids.addFirst("");

        return SUCCESS;
    }

    private LocalFileSystem getFileSystem()
    {
        return new LocalFileSystem(new java.io.File("c:/"));
    }

    private String encode(String uid)
    {
        return new String(Base64.encodeBase64(uid.getBytes()));
    }

}
