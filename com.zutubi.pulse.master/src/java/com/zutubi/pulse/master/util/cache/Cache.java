package com.zutubi.pulse.master.util.cache;

/**
 * The Cache abstracts the cache implementation.
 *
 */
public interface Cache
{
    /**
     * The name of the cache uniquely identifies this cache.
     *
     * @return the name of the cache.
     */
    String getName();

    /**
     * Retrieve an object from this cache.
     * @param key uniquely identifying the object to be retrieved.
     *
     * @return the object from the cache, or null if the object is not found.
     */
    Object get(Object key);

    /**
     * Put an object into the cache. If the specified key already exists within the cache, it will be replaced by
     * the new object.
     *
     * @param key uniquely identifying the object to be added into the cache.
     * @param value to be cached.
     */
    void put(Object key, Object value);

    /**
     * Remove the object identified by the key from the cache. If no object can be found associated with this key
     * then no action is taken.
     *
     * @param key uniquely identifying the object to be removed.
     */
    void remove(Object key);

    /**
     * Remove all of the objects from this cache.
     */
    void removeAll();
}