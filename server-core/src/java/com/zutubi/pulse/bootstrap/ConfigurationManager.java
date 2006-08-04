package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.EnvConfig;

/**
 */
public interface ConfigurationManager
{
    EnvConfig getEnvConfig();
    SystemConfiguration getSystemConfig();
    UserPaths getUserPaths();
    SystemPaths getSystemPaths();
}
