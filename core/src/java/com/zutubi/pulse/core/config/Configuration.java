package com.zutubi.pulse.core.config;

import com.zutubi.config.annotations.Transient;

/**
 * Basic interface that must be implemented by all configuration types.
 */
public interface Configuration
{
    @Transient
    long getHandle();
    void setHandle(long handle);

    @Transient
    String getConfigurationPath();
    void setConfigurationPath(String configurationPath);
}
