package com.zutubi.pulse.core.config;

/**
 * Convenient base class for configuration types.
 */
public abstract class AbstractConfiguration implements Configuration
{
    private long handle;
    private String configurationPath;

    public long getHandle()
    {
        return handle;
    }

    public void setHandle(long handle)
    {
        this.handle = handle;
    }

    public String getConfigurationPath()
    {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath)
    {
        this.configurationPath = configurationPath;
    }
}
