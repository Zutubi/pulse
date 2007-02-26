package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.annotation.TextArea;

/**
 *
 *
 */
public class LicenseConfiguration
{
    private String key;

    @TextArea()
    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }
}
