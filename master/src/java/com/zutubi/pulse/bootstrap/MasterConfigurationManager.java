package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.database.DatabaseConfig;

import java.io.File;
import java.io.IOException;

/**
 * 
 *
 */
public interface MasterConfigurationManager extends ConfigurationManager, DataResolver
{
    public static final String CONFIG_DIR = ".pulse2";
    
    MasterUserPaths getUserPaths();

    void setPulseData(File pulseHome);

    File getDataDirectory();

    Data getData();

    File getHomeDirectory();

    File getDatabaseConfigFile();
    DatabaseConfig getDatabaseConfig() throws IOException;
}
