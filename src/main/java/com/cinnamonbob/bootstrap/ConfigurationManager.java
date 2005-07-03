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
    Config getAppConfig();

    /**
     * 
     * @return application paths.
     */
    ApplicationPaths getApplicationPaths();
}
