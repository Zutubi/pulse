package com.zutubi.pulse.bootstrap;

/**
 */
public interface CoreConfigurationManager
{
    CoreApplicationConfiguration getAppConfig();
    CoreUserPaths getUserPaths();
    SystemPaths getSystemPaths();
}
