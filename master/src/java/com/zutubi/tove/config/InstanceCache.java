package com.zutubi.tove.config;

import com.zutubi.pulse.core.config.Configuration;

import java.util.Collection;

/**
 * A cache of configuration instances.  These instances are shared and thus
 * should be treated as read-only.
 */
public interface InstanceCache extends InstanceSource
{
    void markInvalid(String path);

    boolean isValid(String path, boolean allowIncomplete);

    Collection<Configuration> getAllDescendents(String path, boolean allowIncomplete);

    void getAllMatchingPathPattern(String path, Collection<Configuration> result, boolean allowIncomplete);
    <T extends Configuration> void getAllMatchingPathPattern(String path, Class<T> clazz, Collection<T> result, boolean allowIncomplete);

    void put(String path, Configuration instance, boolean complete);

    void forAllInstances(InstanceHandler handler, boolean allowIncomplete);

    void clear();

    public static interface InstanceHandler
    {
        void handle(Configuration instance, String path, boolean complete, Configuration parentInstance);
    }
}
