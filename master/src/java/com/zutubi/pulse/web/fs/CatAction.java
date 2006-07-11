package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.web.ActionSupport;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;

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
//        contentType = "text/plain";

        // open the input stream.
//        inputStream = new FileInputStream(new File("C:\\projects\\trunk\\velocity.log"));

        // sorted.

        return super.execute();
    }
}
