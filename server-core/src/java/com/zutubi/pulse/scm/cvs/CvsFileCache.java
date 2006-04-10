package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.util.Constants;
import com.zutubi.pulse.filesystem.remote.CachingRemoteFile;
import com.zutubi.pulse.scm.SCMException;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class CvsFileCache
{
    private static Lock instanceLock = new ReentrantLock();
    private static CvsFileCache INSTANCE = null;

    private Map<String, CacheItem> cache;
    private Lock lock = new ReentrantLock();

    private CvsFileCache()
    {
        cache = new TreeMap<String, CacheItem>();
    }

    public static CvsFileCache getInstance()
    {
        instanceLock.lock();
        if (INSTANCE == null)
        {
            INSTANCE = new CvsFileCache();
        }
        instanceLock.unlock();
        return INSTANCE;
    }

    public Map<String, CachingRemoteFile> lookup(CvsServer server) throws SCMException
    {
        CacheItem item;

        lock.lock();
        try
        {
            String uid = getUID(server);
            if (cache.containsKey(uid))
            {
                item = cache.get(uid);
                if (aged(item) && server.hasChangedSince(item.cachedRevision))
                {
                    server.populateCache(item);
                }
            }
            else
            {
                item = new CacheItem();
                server.populateCache(item);
                cache.put(uid, item);
            }
        }
        finally
        {
            lock.unlock();
        }

        return item.cachedListing;
    }

    private boolean aged(CacheItem item)
    {
        return System.currentTimeMillis() - item.cachedRevision.getDate().getTime() > Constants.MINUTE * 5;
    }

    private String getUID(CvsServer server)
    {
        return server.getLocation();
    }

    class CacheItem
    {
        public Map<String, CachingRemoteFile> cachedListing;
        public CvsRevision cachedRevision;
    }
}
