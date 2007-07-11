package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;

import java.util.Collection;

/**
 * A cache of configuration instances.  These instances are shared and thus
 * should be treated as read-only.
 */
public interface InstanceCache
{
    Configuration get(String path);

    Collection<Configuration> getAllDescendents(String path);

    void getAllMatchingPathPattern(String path, Collection<Configuration> result);

    void put(String path, Configuration instance);

    void forAllInstances(InstanceHandler handler);

    void clear();

    public static interface InstanceHandler
    {
        void handle(Configuration instance, String path, Configuration parentInstance);
    }
}
