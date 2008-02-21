package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;

/**
 */
public interface InstanceSource
{
    Configuration getInstance(String path);
}
