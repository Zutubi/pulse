package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class ProjectMapType extends MapType
{
    public ProjectMapType(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        super(configurationPersistenceManager);
    }

    public boolean isTemplated()
    {
        return true;
    }
}
