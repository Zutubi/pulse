package com.zutubi.i18n.context;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The default implementation of the context cache interface.
 * 
 */
public class DefaultContextCache implements ContextCache
{
    private Map<Long, List<ResourceBundle>> contextCache;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Construct cache with default settings
     */
    public DefaultContextCache()
    {
        contextCache = new HashMap<Long, List<ResourceBundle>>();
    }

    /**
     * Add bundle and context to cache
     *
     * @param context context with the scope of the bundle
     * @param locale  locale for the bundle
     * @param bundles  bundle to add
     */
    public void addToCache(Context context, Locale locale, List<ResourceBundle> bundles)
    {
        try
        {
            lock.writeLock().lock();
            contextCache.put(ContextKey.generate(context, locale), bundles);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieve a cached bundle
     *
     * @param context context for the wanted bundle
     * @param locale  locale for the bundle
     * @return cached bundle name
     */
    public List<ResourceBundle> getFromCache(Context context, Locale locale)
    {
        try
        {
            lock.readLock().lock();
            return contextCache.get(ContextKey.generate(context, locale));
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Check if a bundle is cached
     *
     * @param context context to check if it is cached
     * @param locale  locale for the bundle
     * @return true if the context is cached
     */
    public boolean isCached(Context context, Locale locale)
    {
        try
        {
            lock.readLock().lock();
            return contextCache.containsKey(ContextKey.generate(context, locale));
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Clear the cache
     */
    public void clear()
    {
        try
        {
            lock.writeLock().lock();
            try
            {
                lock.writeLock().lock();
                contextCache.clear();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

}
