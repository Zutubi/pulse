/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

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

    void setPulseHome(File pulseHome);

    File getHomeDirectory();

    Home getHome();

    /**
     * Returns true if the system is not completely configured.
     *
     * @return true if further configuration is required.
     */
    boolean requiresSetup();

    File getInstallDirectory();
}
