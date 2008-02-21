package com.zutubi.prototype.config;

import com.zutubi.prototype.type.TypeException;
import com.zutubi.pulse.core.config.Configuration;

/**
 * An instantiator used for deep cloning instances.  Handles the complication
 * of references.
 */
public class CloningInstantiator extends PersistentInstantiator
{
    private String cloneRootPath;

    public CloningInstantiator(String cloneRootPath, String path, boolean concrete, InstanceCache cache, InstanceCache incompleteCache, ConfigurationTemplateManager configurationTemplateManager, ConfigurationReferenceManager configurationReferenceManager)
    {
        super(path, concrete, cache, incompleteCache, configurationTemplateManager, configurationReferenceManager);
        this.cloneRootPath = cloneRootPath;
    }

    protected PersistentInstantiator createChildInstantiator(String propertyPath)
    {
        return new CloningInstantiator(cloneRootPath, propertyPath, concrete, cache, incompleteCache, configurationTemplateManager, configurationReferenceManager);
    }

    public Configuration resolveReference(long toHandle) throws TypeException
    {
        InstanceSource source = configurationTemplateManager;
        String targetPath = configurationReferenceManager.getPathForHandle(toHandle);
        if(targetPath.startsWith(cloneRootPath))
        {
            // This reference points within the object tree we are cloning.
            // We must update it to point to a new clone.
            source = new InstanceSource()
            {
                public Configuration getInstance(String path)
                {
                    Configuration instance = cache.get(path);
                    if(instance == null)
                    {
                        instance = incompleteCache.get(path);
                    }

                    return instance;
                }
            };
        }

        return configurationReferenceManager.resolveReference(path, toHandle, this, source);
    }
}
