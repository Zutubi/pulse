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

    /**
     * 
     * @return application paths.
     */
    ApplicationPaths getApplicationPaths();
}
