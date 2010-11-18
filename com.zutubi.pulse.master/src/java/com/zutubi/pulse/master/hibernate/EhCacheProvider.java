package com.zutubi.pulse.master.hibernate;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.util.cache.ehcache.EhCacheManager;
import org.hibernate.cache.*;

import java.util.Properties;

/**
 * An implementation of the hibernate cache provider interface.
 *
 * This implementation allows us to use our the Pulse systems
 * Ehcache configuration, which uses a custom diskStore path.
 *
 * To install this cache provider implementation, add the following
 * property to the hibernate configuration.
 *
 * 	hibernate.cache.provider_class=com.zutubi.pulse.master.hibernate.EhCacheProvider
 *
 * @see com.zutubi.pulse.master.util.cache.ehcache.EhCacheManager
 * @see org.hibernate.cfg.Environment#CACHE_PROVIDER
 */
public class EhCacheProvider implements CacheProvider
{
    private EhCacheManager cacheManager;

    public EhCacheProvider()
    {
        // We have no control over the construction of this class, so
        // we need to autowire it here.
        SpringComponentContext.autowire(this);
    }

    public boolean isMinimalPutsEnabledByDefault()
    {
        return false;
    }

    public void stop()
    {
        // ignore since all provider implementations use the same
        // manager.
    }

    public void start(Properties properties) throws CacheException
    {
        // noop, we have already configured the manager.
    }

    public long nextTimestamp()
    {
        return Timestamper.next();
    }

    public Cache buildCache(String regionName, Properties properties) throws CacheException
    {
        return new EhCache(cacheManager.getEhCache(regionName));
    }

    public void setCacheManager(EhCacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }
}
