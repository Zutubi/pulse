package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;

/**
 */
public interface InstanceSource
{
    Configuration get(String path, boolean allowIncomplete);
}
