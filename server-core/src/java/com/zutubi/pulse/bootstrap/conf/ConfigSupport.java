package com.zutubi.pulse.bootstrap.conf;

import java.io.File;

/**
 * <class-comment/>
 */
public class ConfigSupport implements Config
{
    private Config config;

    public ConfigSupport(Config config)
    {
        this.config = config;
    }

    public String getProperty(String key)
    {
        return config.getProperty(key);
    }

    public boolean hasProperty(String key)
    {
        return config.hasProperty(key);
    }

    public void removeProperty(String key)
    {
        config.removeProperty(key);
    }

    public void setProperty(String key, String value)
    {
        if (value != null)
        {
            config.setProperty(key, value);
        }
        else
        {
            config.removeProperty(key);
        }
    }

    public int getInt(String key)
    {
        return Integer.parseInt(getProperty(key));
    }

    public void setInt(String key, int value)
    {
        setProperty(key, Integer.toString(value));
    }

    public Integer getInteger(String key)
    {
        if (hasProperty(key))
        {
            return new Integer(getProperty(key));
        }
        return null;
    }

    public void setInteger(String key, Integer value)
    {
        if (value != null)
        {
            setProperty(key, value.toString());
        }
        else
        {
            removeProperty(key);
        }
    }

    public void setFile(String key, File value)
    {
        if (value != null)
        {
            setProperty(key, value.getAbsolutePath());
        }
        else
        {
            removeProperty(key);
        }
    }

    public File getFile(String key)
    {
        if (hasProperty(key))
        {
            return new File(getProperty(key));
        }
        return null;
    }
}
