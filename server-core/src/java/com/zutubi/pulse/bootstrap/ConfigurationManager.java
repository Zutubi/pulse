package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.plugins.PluginPaths;

/**
 */
public interface ConfigurationManager
{
    EnvConfig getEnvConfig();
    SystemConfiguration getSystemConfig();
    UserPaths getUserPaths();
    SystemPaths getSystemPaths();
}
