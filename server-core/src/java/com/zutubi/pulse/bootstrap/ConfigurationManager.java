package com.zutubi.pulse.bootstrap;

/**
 */
public interface ConfigurationManager
{
    SystemConfiguration getSystemConfig();
    UserPaths getUserPaths();
    SystemPaths getSystemPaths();
}
