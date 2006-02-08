package com.cinnamonbob.bootstrap.config;

/**
 * <class-comment/>
 */
public class ReadOnlyConfiguration implements Configuration
{
    private Configuration delegate;

    public ReadOnlyConfiguration(Configuration delegate)
    {
        this.delegate = delegate;
    }

    public String getProperty(String key)
    {
        return delegate.getProperty(key);
    }

    public boolean hasProperty(String key)
    {
        return delegate.hasProperty(key);
    }

    public void removeProperty(String key)
    {
        throw new UnsupportedOperationException();
    }

    public void setProperty(String key, String value)
    {
        throw new UnsupportedOperationException();
    }
}
