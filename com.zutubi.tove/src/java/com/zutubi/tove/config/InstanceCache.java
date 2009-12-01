package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;

import java.util.Collection;
import java.util.Set;

/**
 * A cache of configuration instances.  These instances are shared and thus
 * should be treated as read-only.
 */
public interface InstanceCache
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

    Collection<Configuration> getAllDescendants(String path, boolean allowIncomplete);

    /**
     * Retrieves the instance at the given path, if one exists.
     *
     * @param path            path of the instance to retrieve
     * @param allowIncomplete if true, instances marked incomplete may be
     *                        returned, if false, they may not
     * @return the instance at the given path, or null if no such instance is
     *         found
     */
    Configuration get(String path, boolean allowIncomplete);

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

    /**
     * Gets the set of all paths for instances which make reference to the
     * given path.  The references will be from properties on the instance.
     *
     * @param path the path being referenced
     * @return the set of all referencing instance paths
     *
     * @see #getPropertyPathsReferencing(String)
     */
    Set<String> getInstancePathsReferencing(String path);

    /**
     * Gets the set of all paths for properties which make reference to the
     * given path.  These will be paths to fields, or paths to items of a list
     * field (i.e. ending in a list index).
     *
     * @param path the path being referenced
     * @return the set of all referencing property paths
     *
     * @see #getPropertyPathsReferencing(String)
     */
    Set<String> getPropertyPathsReferencing(String path);

    /**
     * Indexes a reference from a property to an instance.  The property path
     * should be in the same form returned by {@link #getPropertyPathsReferencing(String)}.
     *
     * @param fromPropertyPath path of the property making reference
     * @param toPath           path of the instance being referenced
     */
    void indexReference(String fromPropertyPath, String toPath);

    public static interface InstanceHandler
    {
        void handle(Configuration instance, String path, boolean complete, Configuration parentInstance);
    }
}
