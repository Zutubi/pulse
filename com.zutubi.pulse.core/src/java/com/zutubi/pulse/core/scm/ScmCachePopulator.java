package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;

/**
 */
public interface ScmCachePopulator
{
    String getUniqueLocation(ScmContext context) throws ScmException;

    boolean requiresRefresh(ScmContext context, Revision revision) throws ScmException;

    void populate(ScmContext context, ScmFileCache.CacheItem item) throws ScmException;
}
