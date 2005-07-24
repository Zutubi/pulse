package com.cinnamonbob.model;

import java.util.Properties;
import java.io.*;

/**
 * 
 *
 */
public abstract  class Scm extends Entity
{
    private String name;
    private Project project;
    protected Properties properties = new Properties();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    private String getProperties()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            properties.store(baos, "");
            return baos.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    private void setProperties(String data)
    {
        try
        {
            properties.load(new ByteArrayInputStream(data.getBytes()));
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
