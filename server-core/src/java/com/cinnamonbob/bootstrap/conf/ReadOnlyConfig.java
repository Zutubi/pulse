package com.cinnamonbob.bootstrap.conf;

/**
 * <class-comment/>
 */
public class ReadOnlyConfig implements Config
{
    private Config delegate;

    public ReadOnlyConfig(Config delegate)
    {
        this.delegate = delegate;
    }

    public String getProperty(String key)
    {
        return delegate.getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasProperty(String key)
    {
        return delegate.hasProperty(key);
    }

    public void removeProperty(String key)
    {
        throw new UnsupportedOperationException();
    }
}
