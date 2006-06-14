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

    public String getProperty(String key, String defaultValue)
    {
        if(hasProperty(key))
        {
            return getProperty(key);
        }
        return defaultValue;
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

    public int getInt(String key, int defaultValue)
    {
        if(hasProperty(key))
        {
            return getInt(key);
        }
        else
        {
            return defaultValue;
        }
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

    public Boolean getBooleanProperty(String key, Boolean defaultValue)
    {
        if (hasProperty(key))
        {
            return Boolean.valueOf(getProperty(key));
        }
        return defaultValue;
    }

    public void setBooleanProperty(String key, Boolean val)
    {
        if (val != null)
        {
            setProperty(key, val.toString());
        }
        else
        {
            removeProperty(key);
        }
    }

}
