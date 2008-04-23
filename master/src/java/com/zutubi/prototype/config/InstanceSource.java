package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;

/**
 */
public interface InstanceSource
{
    Configuration get(String path, boolean allowIncomplete);
}
