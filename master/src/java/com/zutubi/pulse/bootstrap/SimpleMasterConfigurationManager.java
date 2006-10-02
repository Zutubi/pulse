package com.zutubi.pulse.bootstrap;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.conf.Config;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.bootstrap.conf.VolatileReadOnlyConfig;

import java.io.File;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class SimpleMasterConfigurationManager extends AbstractConfigurationManager implements MasterConfigurationManager
{
    private SystemConfiguration sysConfig;

    private MasterConfiguration appConfig;

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
            String configPath = envConfig.getDefaultPulseConfig();
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

    public MasterConfiguration getAppConfig()
    {
        if (appConfig == null)
        {
            MasterUserPaths paths = getUserPaths();
            if (paths == null)
            {
                // default values only.
                return new MasterConfigurationSupport();
            }
            Config user = new FileConfig(new File(paths.getUserConfigRoot(), "pulse.properties"));
            appConfig = new MasterConfigurationSupport(user);
        }
        return appConfig;
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

        // refresh the data instance.
        data = null;
    }
}
