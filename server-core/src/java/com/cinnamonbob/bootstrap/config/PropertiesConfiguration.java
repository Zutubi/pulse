package com.cinnamonbob.bootstrap.config;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class PropertiesConfiguration implements Configuration, Editable
{
    private final Properties props;

    public PropertiesConfiguration()
    {
        this(null);
    }

    public PropertiesConfiguration(Properties props)
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
}
