package com.cinnamonbob.bootstrap.config;

import java.io.File;

/**
 * <class-comment/>
 */
public class ConfigurationSupport implements Configuration
{
    private Configuration config;

    public ConfigurationSupport(Configuration config)
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
        config.setProperty(key, value);
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
