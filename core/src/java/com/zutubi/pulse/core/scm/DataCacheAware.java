package com.zutubi.pulse.core.scm;

import java.util.Map;

/**
 *
 *
 */
public interface DataCacheAware
{
    String getCacheId();

    void setCache(Map<Object, Object> cache);
}
