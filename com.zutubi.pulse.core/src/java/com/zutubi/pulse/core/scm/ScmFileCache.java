/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;

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

    public Map<String, CachingScmFile> lookup(ScmContext context, ScmCachePopulator populator) throws ScmException
    {
        CacheItem item;

        lock.lock();
        try
        {
            String uid = populator.getUniqueLocation(context);
            if (cache.containsKey(uid))
            {
                item = cache.get(uid);
                if (populator.requiresRefresh(context, item.cachedRevision))
                {
                    populator.populate(context, item);
                }
            }
            else
            {
                item = new CacheItem();
                populator.populate(context, item);
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
