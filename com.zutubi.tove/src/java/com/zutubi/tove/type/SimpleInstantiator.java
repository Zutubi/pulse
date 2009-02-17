package com.zutubi.tove.type;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.ReferenceResolver;
import com.zutubi.tove.config.api.Configuration;

/**
 * A property instantiator that applies no extra logic to the instantiation
 * process.
 */
public class SimpleInstantiator implements Instantiator
{
    private String templateOwnerPath;
    private ReferenceResolver referenceResolver;
    private ConfigurationTemplateManager configurationTemplateManager;

    /**
     * Create a new instantiator for building throw-away instances.
     *
     * @param templateOwnerPath if in a templated scope, the item of the
     *                          templated collection that owns the object being
     *                          instantiated, otherwise null
     * @param referenceResolver used to resolve references during instantiation
     * @param configurationTemplateManager required resource
     */
    public SimpleInstantiator(String templateOwnerPath, ReferenceResolver referenceResolver, ConfigurationTemplateManager configurationTemplateManager)
    {
        this.templateOwnerPath = templateOwnerPath;
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
        return referenceResolver.resolveReference(templateOwnerPath, toHandle, this, null);
    }
}
