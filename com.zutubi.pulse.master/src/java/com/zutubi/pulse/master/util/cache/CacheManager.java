package com.zutubi.pulse.master.util.cache;

/**
 * <class comment/>
 */
public interface CacheManager
{
    /**
     * Retrieve the cache with a particular name
     *
     * @param cacheName the unique name of the cache
     * @return the appropriate cache
     */
    Cache getCache(String cacheName);

    /**
     * Flush the contents of all caches registered with the cache manager
     */
    void flushCaches();
}
