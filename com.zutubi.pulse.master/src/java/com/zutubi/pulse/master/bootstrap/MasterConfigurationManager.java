package com.zutubi.pulse.master.bootstrap;

import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.database.DriverRegistry;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Extended configuration only present on the Pulse master (not agents).  The main additions are
 * the database and artifact repositories.
 */
public interface MasterConfigurationManager extends ConfigurationManager, DataResolver
{
    String CONFIG_DIR = ".pulse2";
    
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
