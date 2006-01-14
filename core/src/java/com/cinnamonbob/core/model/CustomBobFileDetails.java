package com.cinnamonbob.core.model;

import com.cinnamonbob.core.BuildException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 */
public class CustomBobFileDetails extends BobFileDetails implements BobFileSource
{
    private String bobFileName;

    public CustomBobFileDetails()
    {

    }

    public CustomBobFileDetails(String bobFileName)
    {
        this.bobFileName = bobFileName;
    }

    public BobFileSource getSource()
    {
        return this;
    }

    public InputStream getBobFile(File workDir)
    {
        try
        {
            return new FileInputStream(new File(workDir, bobFileName));
        }
        catch (FileNotFoundException e)
        {
            throw new BuildException(e.getMessage(), e);
        }
    }

    public String getBobFileName()
    {
        return bobFileName;
    }

    public void setBobFileName(String bobFileName)
    {
        this.bobFileName = bobFileName;
    }
}
