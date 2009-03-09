package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;

import java.util.Collection;

/**
 * A cache of configuration instances.  These instances are shared and thus
 * should be treated as read-only.
 */
public interface InstanceCache extends InstanceSource
{
    /**
     * Returns a copy of this cache which points to the same instances.  That
     * is, the cache structure itself is copied, but the same instances are
     * held in the new cache.
     *
     * @return a copy of this cache holding the same instances
     */
    DefaultInstanceCache copyStructure();

    void markInvalid(String path);

    boolean isValid(String path, boolean allowIncomplete);

    Collection<Configuration> getAllDescendents(String path, boolean allowIncomplete);

    void getAllMatchingPathPattern(String path, Collection<Configuration> result, boolean allowIncomplete);
    <T extends Configuration> void getAllMatchingPathPattern(String path, Class<T> clazz, Collection<T> result, boolean allowIncomplete);

    void put(String path, Configuration instance, boolean complete);

    void forAllInstances(InstanceHandler handler, boolean allowIncomplete);

    /**
     * Marks an instance as dirty: i.e. the instance or something it reaches
     * has changed and needs refreshing.
     *
     * @param path the path to mark dirty
     * @return if an entry wsa found at the path and was newly-marked as dirty
     */
    boolean markDirty(String path);

    /**
     * Clears all dirty instances from the cache, ready for them to be
     * refreshed.
     */
    void clearDirty();

    public static interface InstanceHandler
    {
        void handle(Configuration instance, String path, boolean complete, Configuration parentInstance);
    }
}
