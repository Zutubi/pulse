/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.filesystem.remote.CachingRemoteFile;
import com.zutubi.pulse.scm.cvs.CvsServer;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class SCMFileCache
{
    private static Lock instanceLock = new ReentrantLock();
    private static SCMFileCache INSTANCE = null;

    private Map<String, CacheItem> cache;
    private Lock lock = new ReentrantLock();

    private SCMFileCache()
    {
        cache = new TreeMap<String, CacheItem>();
    }

    public static SCMFileCache getInstance()
    {
        instanceLock.lock();
        if (INSTANCE == null)
        {
            INSTANCE = new SCMFileCache();
        }
        instanceLock.unlock();
        return INSTANCE;
    }

    public Map<String, CachingRemoteFile> lookup(SCMCachePopulator populator) throws SCMException
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

    private String getUID(CvsServer server)
    {
        return server.getLocation();
    }

    public class CacheItem
    {
        public Map<String, CachingRemoteFile> cachedListing;
        public Revision cachedRevision;
    }
}
