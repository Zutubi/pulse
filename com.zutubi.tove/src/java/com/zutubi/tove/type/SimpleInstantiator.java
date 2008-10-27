package com.zutubi.tove.type;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.ReferenceResolver;

/**
 * A property instantiator that applies no extra logic to the instantiation
 * process.
 */
public class SimpleInstantiator implements Instantiator
{
    private ReferenceResolver referenceResolver;
    private ConfigurationTemplateManager configurationTemplateManager;

    public SimpleInstantiator(ReferenceResolver referenceResolver, ConfigurationTemplateManager configurationTemplateManager)
    {
        this.referenceResolver = referenceResolver;
        this.configurationTemplateManager = configurationTemplateManager;
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
            if (instance instanceof Configuration)
            {
                configurationTemplateManager.wireIfRequired((Configuration) instance);
            }

            type.initialise(instance, data, this);
        }
        return instance;
    }

    public Configuration resolveReference(long toHandle) throws TypeException
    {
        return referenceResolver.resolveReference(null, toHandle, this);
    }
}
