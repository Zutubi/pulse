package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;

import java.util.Collection;

/**
 * A cache of configuration instances.  These instances are shared and thus
 * should be treated as read-only.
 */
public interface InstanceCache
{
    void markInvalid(String path);

    boolean isValid(String path, boolean allowIncomplete);

    Configuration get(String path, boolean allowIncomplete);

    Collection<Configuration> getAllDescendents(String path, boolean allowIncomplete);

    void getAllMatchingPathPattern(String path, Collection<Configuration> result, boolean allowIncomplete);

    void put(String path, Configuration instance, boolean complete);

    void forAllInstances(InstanceHandler handler, boolean allowIncomplete);

    void clear();

    public static interface InstanceHandler
    {
        void handle(Configuration instance, String path, boolean complete, Configuration parentInstance);
    }
}
