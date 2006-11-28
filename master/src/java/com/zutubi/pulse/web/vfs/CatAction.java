package com.zutubi.pulse.web.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import java.io.InputStream;

/**
 * <class comment/>
 */
public class CatAction extends VFSActionSupport
{
    private String root;
    private String path;

    private InputStream inputStream;

    private String contentType;

    private String filename;

    public String getFilename()
    {
        return filename;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws FileSystemException
    {
        FileObject fo = getFS().resolveFile(root + path);

        // can only cat a file if it is readable.
        if (!fo.isReadable())
        {
            return ERROR;
        }

        filename = fo.getName().getBaseName();
        contentType = fo.getContent().getContentInfo().getContentType();
        inputStream = fo.getContent().getInputStream();

        return SUCCESS;
    }
}
