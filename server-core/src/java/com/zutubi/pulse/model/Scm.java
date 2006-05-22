/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;

import java.util.Properties;

/**
 * 
 *
 */
public abstract class Scm extends Entity implements Cloneable
{
    private String path;
    private Properties properties;

    public abstract SCMServer createServer() throws SCMException;

    public boolean supportsUpdate()
    {
        try
        {
            return createServer().supportsUpdate();
        }
        catch (SCMException e)
        {
            return false;
        }
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

    public Scm copy()
    {
        try
        {
            Scm copy = (Scm) clone();
            // Deep copy the properties
            copy.properties = new Properties();
            copy.properties.putAll(properties);
            return copy;
        }
        catch (CloneNotSupportedException e)
        {
            // Never happens
        }

        return null;
    }
}
