package com.zutubi.pulse.master.bootstrap;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.DataDirectoryChangedEvent;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.database.DriverRegistry;
import com.zutubi.pulse.servercore.bootstrap.AbstractConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.bootstrap.SystemConfigurationSupport;
import com.zutubi.util.StringUtils;
import com.zutubi.util.config.Config;
import com.zutubi.util.config.FileConfig;
import com.zutubi.util.config.VolatileReadOnlyConfig;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 */
public class SimpleMasterConfigurationManager extends AbstractConfigurationManager implements MasterConfigurationManager
{
    public static final String CORE_PROPERTY_PULSE_DATABASE_URL = "pulse.database.url";
    public static final String CORE_PROPERTY_PULSE_LOG_DIR = "pulse.log.dir";
    public static final String DATABASE_CONFIG_FILE_PREFIX = "database";

    private static final Logger LOG = Logger.getLogger(SimpleMasterConfigurationManager.class);

    private SystemConfiguration sysConfig;
    private DatabaseConfig dbConfig;
    private DriverRegistry driverRegistry;
    private Data data;

    public SystemConfiguration getSystemConfig()
    {
        if (sysConfig == null)
        {
            EnvConfig envConfig = getEnvConfig();

            // command line configuration only.
            Properties systemCopy = new Properties();
            systemCopy.putAll(System.getProperties());
            Config system = new VolatileReadOnlyConfig(systemCopy);

            // look for the external user configuration file.
            String configPath = envConfig.getDefaultPulseConfig(CONFIG_DIR);
            if (envConfig.hasPulseConfig())
            {
                configPath = envConfig.getPulseConfig();
            }

            File userProps = new File(configPath);
            if (!userProps.isFile())
            {
                // user config not yet available, so just use the defaults for now.
                return new SystemConfigurationSupport(configPath, system);
            }
            Config user = new FileConfig(userProps);
            sysConfig = new SystemConfigurationSupport(configPath, system, user);
        }
        return sysConfig;
    }

    public MasterUserPaths getUserPaths()
    {
        return getData();
    }

    public File getDataDirectory()
    {
        return new File(getSystemConfig().getDataPath());
    }

    public File getHomeDirectory()
    {
        if (envConfig.hasPulseHome())
        {
            return new File(envConfig.getPulseHome());
        }
        // this is expected in a dev environment.
        return null;
    }

    public File getDatabaseConfigFile()
    {
        MasterUserPaths userPaths = getUserPaths();
        if (userPaths != null)
        {
            return new File(userPaths.getUserConfigRoot(), DATABASE_CONFIG_FILE_PREFIX +".properties");
        }
        return null;
    }

    public DatabaseConfig getDatabaseConfig() throws IOException
    {
        if (dbConfig == null)
        {
            Properties p = new Properties();

            if (getUserPaths() != null && getUserPaths().getUserConfigRoot() != null)
            {
                File configFile = getDatabaseConfigFile();
                if (configFile.exists())
                {
                    p.putAll(IOUtils.read(configFile));
                }

                configFile = new File(getUserPaths().getUserConfigRoot(), DATABASE_CONFIG_FILE_PREFIX +".user.properties");
                if (configFile.exists())
                {
                    p.putAll(IOUtils.read(configFile));
                }
            }

            dbConfig = new DatabaseConfig(p);
            dbConfig.setUserPaths(getUserPaths());
        }
        return dbConfig;
    }

    public void updateDatabaseConfig(Properties updatedProperties) throws IOException
    {
        File configFile = getDatabaseConfigFile();
        if (configFile != null)
        {
            IOUtils.write(updatedProperties, configFile);

            // trigger a reload on the next get.
            dbConfig = null;
        }
    }

    public DriverRegistry getDriverRegistry()
    {
        if (driverRegistry == null)
        {
            synchronized(this)
            {
                if (driverRegistry == null)
                {
                    driverRegistry = new DriverRegistry();
                    driverRegistry.setDriverDir(getData().getDriverRoot());
                    driverRegistry.init();
                }
            }
        }
        return driverRegistry;
    }

    public Data getData()
    {
        if (data == null)
        {
            if (!StringUtils.stringSet(getSystemConfig().getDataPath()))
            {
                return null;
            }
            data = new Data(new File(getSystemConfig().getDataPath()));
        }
        return data;
    }

    public void setPulseData(File f)
    {
        getSystemConfig().setDataPath(f.getAbsolutePath());

        // this object is instantiated before the eventManager, so the wiring needs to be manual.
        EventManager eventManager = SpringComponentContext.getBean("eventManager");
        eventManager.publish(new DataDirectoryChangedEvent(this));

        // refresh the data instance.
        data = null;
    }

    @Override
    public Map<String, String> getCoreProperties()
    {
        Map<String, String> result = super.getCoreProperties();
        try
        {
            result.put(CORE_PROPERTY_PULSE_DATABASE_URL, getDatabaseConfig().getUrl());
            result.put(CORE_PROPERTY_PULSE_LOG_DIR, getSystemPaths().getLogRoot().getAbsolutePath());
        }
        catch (IOException e)
        {
            LOG.warning(e);
        }

        return result;
    }
}
