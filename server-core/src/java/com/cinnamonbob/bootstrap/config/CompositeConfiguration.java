package com.cinnamonbob.bootstrap.config;

import java.util.Arrays;
import java.util.List;

/**
 * <class-comment/>
 */
public class CompositeConfiguration implements Configuration
{
    private List<Configuration> configurations;

    private Configuration writeable;

    public CompositeConfiguration(Configuration... configurations)
    {
        this.configurations = Arrays.asList(configurations);
        for (Configuration config : configurations)
        {
            if (config instanceof Editable)
            {
                writeable = config;
                break;
            }
        }
    }

    public String getProperty(String key)
    {
        for (Configuration config: configurations)
        {
            if (config.hasProperty(key))
            {
                return config.getProperty(key);
            }
        }
        return null;
    }

    public boolean hasProperty(String key)
    {
        return getProperty(key) != null;
    }

    public void removeProperty(String key)
    {
        Configuration config = locateWritableConfiguration(key);
        if (config == null)
        {
            throw new UnsupportedOperationException();
        }
        config.removeProperty(key);
    }

    public void setProperty(String key, String value)
    {
        Configuration config = locateWritableConfiguration(key);
        if (config == null)
        {
            throw new UnsupportedOperationException();
        }
        config.setProperty(key, value);
    }

    private Configuration locateWritableConfiguration(String key)
    {
        if (writeable == null)
        {
            return null;
        }
        for (Configuration config: configurations)
        {
            // check if there are any read only layers above the writeable config
            // that contain the property. if so, the property becomes read only.
            if (config != writeable  && config.hasProperty(key))
            {
                return null;
            }
            if (config == writeable)
            {
                return config;
            }
        }
        return null;
    }
}
