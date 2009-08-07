package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.util.StringUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.UriParser;

import java.io.InputStream;

/**
 * <class comment/>
 */
public class CatAction extends VFSActionSupport
{
    /**
     * @deprecated use path instead.
     */
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
        if (StringUtils.stringSet(root))
        {
            path = root + path;
        }
        
        FileObject fo = getFS().resolveFile(path);

        // can only cat a file if it is readable.
        if (!fo.isReadable())
        {
            addActionError("You do not have permission to view this file.");
            return ERROR;
        }

        filename = UriParser.decode(fo.getName().getBaseName());
        contentType = fo.getContent().getContentInfo().getContentType();
        inputStream = fo.getContent().getInputStream();

        return SUCCESS;
    }
}
