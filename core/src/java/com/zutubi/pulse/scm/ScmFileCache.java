package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.ScmException;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class ScmFileCache
{
    private static Lock instanceLock = new ReentrantLock();
    private static ScmFileCache INSTANCE = null;

    private Map<String, CacheItem> cache;
    private Lock lock = new ReentrantLock();

    private ScmFileCache()
    {
        cache = new TreeMap<String, CacheItem>();
    }

    public static ScmFileCache getInstance()
    {
        instanceLock.lock();
        if (INSTANCE == null)
        {
            INSTANCE = new ScmFileCache();
        }
        instanceLock.unlock();
        return INSTANCE;
    }

    public Map<String, CachingScmFile> lookup(ScmCachePopulator populator) throws ScmException
    {
        CacheItem item;

        lock.lock();
        try
        {
            String uid = populator.getUniqueLocation();
            if (cache.containsKey(uid))
            {
                item = cache.get(uid);
                if (populator.requiresRefresh(item.cachedRevision))
                {
                    populator.populate(item);
                }
            }
            else
            {
                item = new CacheItem();
                populator.populate(item);
                cache.put(uid, item);
            }
        }
        finally
        {
            lock.unlock();
        }

        return item.cachedListing;
    }

    public class CacheItem
    {
        public Map<String, CachingScmFile> cachedListing;
        public Revision cachedRevision;
    }
}
