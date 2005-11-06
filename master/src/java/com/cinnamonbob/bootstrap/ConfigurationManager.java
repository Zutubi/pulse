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

    public boolean hasProperty(String key);
    public String lookupProperty(String key);
    
    
    /**
     * 
     * @return application paths.
     */
    ApplicationPaths getApplicationPaths();
}
