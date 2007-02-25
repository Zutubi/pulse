package com.zutubi.pulse.cache.ehcache;

import org.acegisecurity.acl.basic.cache.EhCacheBasedAclEntryCache;

/**
 */
public class CustomAclEntryCache extends EhCacheBasedAclEntryCache
{
    private EhCacheManager cacheManager;
    private EhCache pulseCache;

    public void afterPropertiesSet() throws Exception
    {
        pulseCache = cacheManager.getCache("AclEntries");
        setCache(pulseCache.getDelegate());
        super.afterPropertiesSet();
    }

    public void setCacheManager(EhCacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }
}
