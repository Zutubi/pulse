package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.ScmException;

/**
 */
public interface ScmCachePopulator
{
    String getUniqueLocation() throws ScmException;

    boolean requiresRefresh(Revision revision) throws ScmException;

    void populate(ScmFileCache.CacheItem item) throws ScmException;
}
