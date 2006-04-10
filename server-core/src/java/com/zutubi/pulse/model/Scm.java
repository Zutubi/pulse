package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;

import java.util.Properties;

/**
 * 
 *
 */
public abstract class Scm extends Entity
{
    private String path;
    private Properties properties;

    public abstract SCMServer createServer() throws SCMException;

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
