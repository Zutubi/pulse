package com.cinnamonbob.bootstrap;

import java.io.File;

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

    void setBobHome(File bobHome);

    File getBobHome();

    Home getHome();

    /**
     * Returns true if the system is not completely configured.
     *
     * @return true if further configuration is required.
     */
    boolean requiresSetup();
}
