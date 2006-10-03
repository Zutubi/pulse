package com.zutubi.pulse.config;

/**
 * <class-comment/>
 */
public class ConfigSupport implements Config
{
    protected Config config;

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

    public boolean isWriteable()
    {
        return config.isWriteable();
    }

    public Integer getInteger(String key)
    {
        return getInteger(key, null);
    }

    public Integer getInteger(String key, Integer defaultValue)
    {
        if (hasProperty(key))
        {
            return Integer.valueOf(getProperty(key));
        }
        return defaultValue;
    }

    public void setInteger(String key, Integer value)
    {
        if (value != null)
        {
            setProperty(key, Integer.toString(value));
        }
        else
        {
            removeProperty(key);
        }
    }

    public Long getLong(String key)
    {
        if (hasProperty(key))
        {
            return Long.valueOf(getProperty(key));
        }
        return null;
    }

    public Long getLong(String key, Long defaultValue)
    {
        if (hasProperty(key))
        {
            return Long.valueOf(getProperty(key));
        }
        return defaultValue;
    }

    public void setLong(String key, Long value)
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
