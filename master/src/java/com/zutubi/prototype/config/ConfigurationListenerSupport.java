package com.zutubi.prototype.config;

/**
 * Do-nothing listener implementation that implementers can override for
 * convenience when not all callbacks are meaningful.
 */
public class ConfigurationListenerSupport implements ConfigurationListener
{
    public void preInsert(String path)
    {
    }

    public void postInsert(String path, String insertedPath, Object newInstance)
    {
    }

    public void preSave(String path, Object oldInstance)
    {
    }

    public void postSave(String path, Object oldInstance, String newPath, Object newInstance)
    {
    }

    public void preDelete(String path, Object instance)
    {
    }

    public void postDelete(String path, Object oldInstance)
    {
    }
}
