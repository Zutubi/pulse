package com.zutubi.pulse.master.bootstrap;

import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.database.DriverRegistry;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

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

    void updateDatabaseConfig(Properties updatedProperties) throws IOException;

    DatabaseConfig getDatabaseConfig() throws IOException;

    DriverRegistry getDriverRegistry();
}
