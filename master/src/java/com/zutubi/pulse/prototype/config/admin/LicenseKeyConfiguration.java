package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.annotation.TextArea;
import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
public class LicenseKeyConfiguration
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
