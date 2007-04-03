package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Revision;

/**
 */
public interface SCMCachePopulator
{
    String getUniqueLocation() throws SCMException;

    boolean requiresRefresh(Revision revision) throws SCMException;

    void populate(SCMFileCache.CacheItem item) throws SCMException;
}
