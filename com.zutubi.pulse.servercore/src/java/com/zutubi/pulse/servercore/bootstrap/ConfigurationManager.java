package com.zutubi.pulse.servercore.bootstrap;

import com.zutubi.pulse.servercore.bootstrap.conf.EnvConfig;

/**
 */
public interface ConfigurationManager
{
    EnvConfig getEnvConfig();
    SystemConfiguration getSystemConfig();
    UserPaths getUserPaths();
    SystemPaths getSystemPaths();
}
