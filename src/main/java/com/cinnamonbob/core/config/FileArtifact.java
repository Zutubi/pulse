package com.cinnamonbob.core.config;

import java.io.*;

/**
 * 
 *
 */
public class FileArtifact extends AbstractArtifact
{
    private File file;
    
    private String title;
    private String type;
    
    public FileArtifact(String name, File file)
    {
        this.name = name;
        this.file = file;
    }
    
    public FileArtifact()
    {
        
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setFile(File file)
    {
        this.file = file;
    }
    
    public InputStream getContent()
    {
        if (!file.exists())
        {
            return new ByteArrayInputStream(new byte[0]);    
        }
            
        try
        {
            return new FileInputStream(file);
        } 
        catch (FileNotFoundException e)
        {
            // will not get here since we have checked that the file exists.
        }
        return null;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setType(String type)
    {
        this.type = type;
    }
    
    public void setFromFile(File f)
    {
        setFile(f);
    }
}
