package com.cinnamonbob.model;

import java.util.Properties;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;

/**
 * 
 *
 */
public abstract class Scm extends Entity
{
    private String name;
    private Project project;
    private String path;
    private Properties properties;

    public abstract SCMServer createServer() throws SCMException;
    
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

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
