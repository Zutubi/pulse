package com.zutubi.prototype.config;

import com.zutubi.prototype.type.Instantiator;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.Configuration;

/**
 * Instantiator that is used when refreshing persistent instance caches in
 * the {@link ConfigurationTemplateManager}.  Handles caching and setting of
 * the handle and path on configuration objects.
 */
public class PersistentInstantiator implements Instantiator
{
    private String path;
    private InstanceCache cache;
    private ConfigurationReferenceManager configurationReferenceManager;

    public PersistentInstantiator(String path, InstanceCache cache, ConfigurationReferenceManager configurationReferenceManager)
    {
        this.path = path;
        this.cache = cache;
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
            PersistentInstantiator childInstantiator = new PersistentInstantiator(propertyPath, cache, configurationReferenceManager);
            instance = type.instantiate(data, childInstantiator);

            if (instance != null)
            {
                if (instance instanceof Configuration)
                {
                    Configuration configuration = (Configuration) instance;
                    configuration.setConfigurationPath(propertyPath);
                    configuration.setHandle(((Record) data).getHandle());
                    cache.put(propertyPath, configuration);
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
