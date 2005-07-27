package com.cinnamonbob.model;

import java.util.Properties;
import java.io.*;

/**
 * 
 *
 */
public abstract class Scm extends Entity
{
    private String name;
    private Project project;
    private Properties properties;

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

    protected Properties getProperties()
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        return properties;
    }
    
    private void setProperties(Properties properties)
    {
        this.properties = properties;
    }

}
