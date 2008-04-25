package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.bootstrap.conf.VolatileReadOnlyConfig;
import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.FileConfig;
import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.events.DataDirectoryChangedEvent;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.util.IOUtils;
import com.zutubi.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 */
public class SimpleMasterConfigurationManager extends AbstractConfigurationManager implements MasterConfigurationManager
{
    private SystemConfiguration sysConfig;
    private DatabaseConfig dbConfig;
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
                return new SystemConfigurationSupport(system);
            }
            Config user = new FileConfig(userProps);
            sysConfig = new SystemConfigurationSupport(system, user);
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
        return new File(getUserPaths().getUserConfigRoot(), "database.properties");
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

                configFile = new File(getUserPaths().getUserConfigRoot(), "database.user.properties");
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

    public Data getData()
    {
        if (data == null)
        {
            if (!TextUtils.stringSet(getSystemConfig().getDataPath()))
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
        EventManager eventManager = (EventManager) ComponentContext.getBean("eventManager");
        eventManager.publish(new DataDirectoryChangedEvent(this));

        // refresh the data instance.
        data = null;
    }
}
