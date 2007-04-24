package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.config.annotations.TextArea;
import com.zutubi.validation.annotations.Required;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
public class LicenseKeyConfiguration extends AbstractConfiguration
{
    @Required @TextArea(rows = 6, cols=50)
    private String key;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }
}
