package com.zutubi.pulse.core.config;

/**
 * Convenient base class for configuration types.
 */
public abstract class AbstractConfiguration implements Configuration
{
    private long id;

    public long getID()
    {
        return id;
    }

    public void setID(long id)
    {
        this.id = id;
    }
}
