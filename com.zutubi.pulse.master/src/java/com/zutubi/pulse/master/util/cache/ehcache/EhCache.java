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

package com.zutubi.pulse.master.util.cache.ehcache;

import com.zutubi.util.logging.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * <class comment/>
 */
public class EhCache implements com.zutubi.pulse.master.util.cache.Cache
{
    private static final Logger LOG = Logger.getLogger(EhCache.class);

    private final Cache delegate;

    public EhCache(Cache cache)
    {
        if (cache == null)
        {
            throw new IllegalArgumentException("Can not create an ehcache wrapper around a null cache instance.");
        }
        this.delegate = cache;
    }

    public String getName()
    {
        return delegate.getName();
    }

    public Object get(Object key)
    {
        try
        {
            Element e = delegate.get((Serializable) key);
            if (e != null)
            {
                return e.getValue();
            }
            return null;
        }
        catch (CacheException e)
        {
            LOG.warning("Error retrieving cache("+getName()+")ed object. Key: '"+key+"'", e);
            return null;
        }
    }

    public List getKeys()
    {
        try
        {
            return delegate.getKeys();
        }
        catch (CacheException e)
        {
            LOG.error("Error looking up the cache("+getName()+") keys. Reason: " + e.getMessage(), e);
            return Collections.EMPTY_LIST;
        }
    }

    public void put(Object key, Object value)
    {
        delegate.put(new Element((Serializable)key, (Serializable)value));
    }

    public void remove(Object key)
    {
        delegate.remove((Serializable) key);
    }

    public void removeAll()
    {
        delegate.removeAll();
    }

    Cache getDelegate()
    {
        return delegate;
    }
}
