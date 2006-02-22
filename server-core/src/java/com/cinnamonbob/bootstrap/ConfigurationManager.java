package com.cinnamonbob.bootstrap;

/**
 * 
 *
 */
public interface ConfigurationManager
{
    /**
     * 
     * @return system configuration.
     */
    ApplicationConfiguration getAppConfig();

    UserPaths getUserPaths();

    SystemPaths getSystemPaths();
}
