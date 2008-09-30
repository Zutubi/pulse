package com.zutubi.tove.config;

import com.zutubi.pulse.core.config.Configuration;

/**
 */
public abstract class TypeAdapter<X extends Configuration> extends TypeListener<X>
{
    public TypeAdapter(Class<X> configurationClass)
    {
        super(configurationClass);
    }

    public void insert(X instance)
    {
        // noop
    }

    public void delete(X instance)
    {
        // noop
    }

    public void save(X instance, boolean nested)
    {
        // noop
    }

    public void postInsert(X instance)
    {
        // noop
    }

    public void postDelete(X instance)
    {
        // noop
    }


    public void postSave(X instance, boolean nested)
    {
        // noop
    }
}
