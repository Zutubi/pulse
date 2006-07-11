package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.web.ActionSupport;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public class CatAction extends ActionSupport
{
    private String path;

    private InputStream inputStream;

    private String contentType;

    public void setPath(String path)
    {
        this.path = path;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String execute() throws Exception
    {
        // locate the file specified by the path.

        // get its content type.

        // open the input stream.

        // sorted.

        return super.execute();
    }
}
