package com.zutubi.pulse.bootstrap;

/**
 */
public interface ConfigurationManager
{
    ApplicationConfiguration getAppConfig();
    UserPaths getUserPaths();
    SystemPaths getSystemPaths();
}
