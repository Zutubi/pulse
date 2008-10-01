package com.zutubi.pulse.servercore.bootstrap.conf;

import com.zutubi.pulse.core.util.config.Config;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class VolatileReadOnlyConfig implements Config
{
    private Properties props = new Properties();

    public VolatileReadOnlyConfig(Properties sysProps)
    {
        this.props.putAll(sysProps);
    }

    public String getProperty(String key)
    {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        removeProperty(key);
    }

    public boolean hasProperty(String key)
    {
        return props.containsKey(key);
    }

    public void removeProperty(String key)
    {
        props.remove(key);
    }

    public boolean isWriteable()
    {
        return true;
    }
}
