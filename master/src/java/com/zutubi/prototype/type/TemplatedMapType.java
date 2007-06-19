package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ConfigurationTemplateManager;

/**
 */
public class TemplatedMapType extends MapType
{
    public TemplatedMapType(ConfigurationTemplateManager configurationTemplateManager)
    {
        super(configurationTemplateManager);
    }

    public boolean isTemplated()
    {
        return true;
    }
}
