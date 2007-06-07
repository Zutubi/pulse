package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ConfigurationTemplateManager;

/**
 */
public class ProjectMapType extends MapType
{
    public ProjectMapType(ConfigurationTemplateManager configurationTemplateManager)
    {
        super(configurationTemplateManager);
    }

    public boolean isTemplated()
    {
        return true;
    }
}
