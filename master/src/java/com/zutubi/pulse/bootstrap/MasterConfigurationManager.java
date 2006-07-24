package com.zutubi.pulse.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public interface MasterConfigurationManager extends ConfigurationManager, DataResolver
{
    /**
     * 
     * @return system configuration.
     */
    MasterConfiguration getAppConfig();

    MasterUserPaths getUserPaths();

    void setPulseData(File pulseHome);

    File getDataDirectory();

    Data getData();

    File getHomeDirectory();
}
