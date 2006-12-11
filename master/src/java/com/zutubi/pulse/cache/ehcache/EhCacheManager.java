package com.zutubi.pulse.cache.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import com.zutubi.pulse.cache.Cache;

/**
 * <class comment/>
 */
public class EhCacheManager implements com.zutubi.pulse.cache.CacheManager
{
    private final CacheManager cacheManager;

    public EhCacheManager()
    {
        try
        {
            cacheManager = CacheManager.create();
        }
        catch (CacheException e)
        {
            throw new RuntimeException(e);
        }
    }

    public synchronized Cache getCache(String name)
    {
        if (!cacheManager.cacheExists(name))
        {
            try
            {
                cacheManager.addCache(name);
            }
            catch (CacheException e)
            {
                throw new RuntimeException(e);
            }
        }
        return new EhCache(cacheManager.getCache(name));
    }

    public void flushCaches()
    {
        for (int i = 0; i < cacheManager.getCacheNames().length; i++)
        {
            String cacheName = cacheManager.getCacheNames()[i];
            getCache(cacheName).removeAll();
        }
    }
}
