package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ReferenceResolver;
import com.zutubi.pulse.core.config.Configuration;

/**
 * A property instantiator that applies no extra logic to the instantiation
 * process.
 */
public class SimpleInstantiator implements Instantiator
{
    private ReferenceResolver referenceResolver;

    public SimpleInstantiator(ReferenceResolver referenceResolver)
    {
        this.referenceResolver = referenceResolver;
    }

    public Object instantiate(String property, boolean relative, Type type, Object data) throws TypeException
    {
        return instantiate(type, data);
    }

    public Object instantiate(Type type, Object data) throws TypeException
    {
        Object instance = type.instantiate(data, this);
        if (instance != null)
        {
            type.initialise(instance, data, this);
        }
        return instance;
    }

    public Configuration resolveReference(long toHandle) throws TypeException
    {
        return referenceResolver.resolveReference(null, toHandle, this);
    }
}
