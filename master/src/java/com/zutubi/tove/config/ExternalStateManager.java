package com.zutubi.tove.config;

import com.zutubi.pulse.core.config.Configuration;

/**
 */
public interface ExternalStateManager<T extends Configuration>
{
    long createState(T instance);
    void rollbackState(long id);
    Object getState(long id);
}
