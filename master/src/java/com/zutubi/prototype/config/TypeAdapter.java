package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;

/**
 *
 *
 */
public abstract class TypeAdapter<X extends Configuration> extends TypeListener<X>
{
    public TypeAdapter(Class<X> configurationClass)
    {
        super(configurationClass);
    }

    public void postInsert(X instance)
    {
        // noop
    }

    public void preDelete(X instance)
    {
        // noop
    }

    public void postSave(X instance)
    {
        // noop
    }
}
