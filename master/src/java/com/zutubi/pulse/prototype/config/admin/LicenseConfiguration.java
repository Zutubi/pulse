package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.TextArea;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("licenseConfig")
public class LicenseConfiguration extends AbstractConfiguration
{
    @TextArea(rows = 10, cols = 80)
    private String key;

    private String name;
    private String type;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

}
