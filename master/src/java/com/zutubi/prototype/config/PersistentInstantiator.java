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
    private InstanceCache incompleteCache;
    private ConfigurationReferenceManager configurationReferenceManager;

    public PersistentInstantiator(String path, boolean concrete, InstanceCache cache, InstanceCache incompleteCache, ConfigurationReferenceManager configurationReferenceManager)
    {
        this.path = path;
        this.concrete = concrete;
        this.cache = cache;
        this.incompleteCache = incompleteCache;
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public Object instantiate(String propertyPath, boolean relative, Type type, Object data) throws TypeException
    {
        if (relative)
        {
            propertyPath = PathUtils.getPath(path, propertyPath);
        }

        Object instance = cache.get(propertyPath);
        if (instance == null)
        {
            PersistentInstantiator childInstantiator = new PersistentInstantiator(propertyPath, concrete, cache, incompleteCache, configurationReferenceManager);
            instance = type.instantiate(data, childInstantiator);

            if (instance != null)
            {
                // If this is a newly-created Configuration (as opposed to a
                // reference), we need to initialise and cache it.
                if (type instanceof ComplexType && instance instanceof Configuration)
                {
                    ComponentContext.autowire(instance);

                    Configuration configuration = (Configuration) instance;
                    configuration.setConfigurationPath(propertyPath);
                    configuration.setHandle(((Record) data).getHandle());

                    if(concrete && configuration.isValid())
                    {
                        cache.put(propertyPath, configuration);
                    }
                    else
                    {
                        incompleteCache.put(propertyPath, configuration);
                    }
                }

                type.initialise(instance, data, childInstantiator);
            }
        }

        return instance;
    }

    public Configuration resolveReference(long toHandle) throws TypeException
    {
        return configurationReferenceManager.resolveReference(path, toHandle, this);
    }
}
