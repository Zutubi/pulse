package com.zutubi.pulse.prototype.config;

import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
public class MavenTypeConfiguration extends AbstractConfiguration
{
    private String target;

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }
}
