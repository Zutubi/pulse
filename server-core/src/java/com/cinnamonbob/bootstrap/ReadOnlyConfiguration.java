package com.cinnamonbob.bootstrap;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class ReadOnlyConfiguration implements Configuration
{
    private final Properties props;

    public ReadOnlyConfiguration(Properties props)
    {
        this.props = props;
    }

    public String getProperty(String key)
    {
        return props.getProperty(key);
    }

    public boolean hasProperty(String key)
    {
        return props.getProperty(key) != null;
    }

    public void resetDefaults()
    {
        throw new UnsupportedOperationException();
    }

    public void setProperty(String key, String value)
    {
        throw new UnsupportedOperationException();
    }
}
