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

    void setPulseData(File pulseHome);

    File getDataDirectory();

    Data getData();

    /**
     * Returns true if the system is not completely configured.
     *
     * @return true if further configuration is required.
     */
    boolean requiresSetup();

    File getHomeDirectory();
}
