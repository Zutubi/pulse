package com.zutubi.pulse.core.scm;

import java.util.Map;

/**
 * The DataCacheAware interface provides scm client implementations with a mechanism to
 * cache data across invocations.
 *
 * Scm Client instances are short lived by design.  Therefore, if an implementation would
 * like to persist information across multiple calls, they should use the datacache.
 *
 */
public interface DataCacheAware
{
    /**
     * The cache id uniquely identifies the data cache available to each instance.  The
     * cache id should be constructed such that it identifies the cache relevant to it.
     *
     * For example, the cvs implementation is configured with a cvsroot, uniquely identifying
     * the remote repository.  If you were to store the version of that remote repository in
     * the data cache, then the cache id would be the cvsroot.  In this way, if the cvsroot
     * changes, the data cache also changes.
     *
     * @return a string uniquely identifying the data cache.
     */
    String getCacheId();

    /**
     * This method is a callback through which the data cache is injected into the data cache
     * aware instance.
     *
     * @param cache instance.
     */
    void setCache(Map<Object, Object> cache);
}
