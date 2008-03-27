package com.zutubi.prototype.config;

import com.zutubi.prototype.type.ComplexType;
import com.zutubi.prototype.type.Instantiator;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.config.Configuration;

/**
 * Instantiator that is used when refreshing persistent instance caches in
 * the {@link ConfigurationTemplateManager}.  Handles caching and setting of
 * the handle and path on configuration objects.
 */
public class PersistentInstantiator implements Instantiator
{
    private String path;
    private boolean concrete;
    private InstanceCache cache;
    private ReferenceResolver referenceResolver;
    private ConfigurationTemplateManager configurationTemplateManager;

    public PersistentInstantiator(String path, boolean concrete, InstanceCache cache, ReferenceResolver referenceResolver, ConfigurationTemplateManager configurationTemplateManager)
    {
        this.path = path;
        this.concrete = concrete;
        this.cache = cache;
        this.referenceResolver = referenceResolver;
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public Object instantiate(String propertyPath, boolean relative, Type type, Object data) throws TypeException
    {
        if (relative)
        {
            propertyPath = PathUtils.getPath(path, propertyPath);
        }

        Object instance = cache.get(propertyPath, true);
        if (instance == null)
        {
            PersistentInstantiator childInstantiator = new PersistentInstantiator(propertyPath, concrete, cache, referenceResolver, configurationTemplateManager);
            instance = type.instantiate(data, childInstantiator);

            if (instance != null)
            {
                // If this is a newly-created Configuration (as opposed to a
                // reference), we need to initialise, cache and validate it.
                if (type instanceof ComplexType && instance instanceof Configuration)
                {
                    ComponentContext.autowire(instance);

                    Configuration configuration = (Configuration) instance;

                    Record record = (Record) data;
                    for(String key: record.metaKeySet())
                    {
                        configuration.putMeta(key, record.getMeta(key));
                    }
                    
                    configuration.setConfigurationPath(propertyPath);
                    configuration.setConcrete(concrete);

                    cache.put(propertyPath, configuration, concrete);
                }

                type.initialise(instance, data, childInstantiator);

//                if (type instanceof CompositeType && instance instanceof Configuration)
//                {
//                    configurationTemplateManager.validateInstance((CompositeType) type, (Configuration) instance, PathUtils.getParentPath(path), PathUtils.getBaseName(path), concrete);
//                }
            }
        }

        return instance;
    }

    public Configuration resolveReference(long toHandle) throws TypeException
    {
        return referenceResolver.resolveReference(path, toHandle, this);
    }
}
