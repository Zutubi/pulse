package com.zutubi.util.config;

import java.util.Properties;

public class PropertiesConfig implements Config
{
    private final Properties props;

    public PropertiesConfig()
    {
        this(null);
    }

    public PropertiesConfig(Properties props)
    {
        if (props == null)
        {
            props = new Properties();
        }
        this.props = props;
    }

    public String getProperty(String key)
    {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        props.setProperty(key, value);
    }

    public boolean hasProperty(String key)
    {
        return props.containsKey(key);
    }

    public void removeProperty(String key)
    {
        props.remove(key);
    }

    public boolean isWritable()
    {
        return true;
    }
}
