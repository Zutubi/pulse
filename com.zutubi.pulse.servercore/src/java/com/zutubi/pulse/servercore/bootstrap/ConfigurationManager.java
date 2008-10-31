package com.zutubi.pulse.servercore.bootstrap;

import com.zutubi.pulse.core.util.config.EnvConfig;

/**
 */
public interface ConfigurationManager
{
    EnvConfig getEnvConfig();
    SystemConfiguration getSystemConfig();
    UserPaths getUserPaths();
    SystemPaths getSystemPaths();
}
