package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.*;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;

/**
 * <class-comment/>
 */
public class SimpleMasterConfigurationManager extends AbstractConfigurationManager implements MasterConfigurationManager
{
    private static final Logger LOG = Logger.getLogger(SimpleMasterConfigurationManager.class);

    private DataConfiguration dataConfig;

    public SystemConfiguration getSystemConfig()
    {
        // system.
        Config system = new VolatileReadOnlyConfig(System.getProperties());

        // user home properties.
        String userHome = System.getProperty("user.home");
        File userProps = new File(new File(new File(userHome), ".pulse"), "pulse.properties");

        // defaults.
        if (userProps.isFile())
        {
            Config user = new FileConfig(userProps);
            return new SystemConfigurationSupport(system, user);
        }
        else
        {
            return new SystemConfigurationSupport(system);
        }
    }

    public MasterConfiguration getAppConfig()
    {
        MasterUserPaths paths = getUserPaths();
        if (paths != null)
        {
            Config user = new FileConfig(new File(paths.getUserConfigRoot(), "pulse.properties"));
            return new MasterConfigurationSupport(user);
        }
        // default values only.
        return new MasterConfigurationSupport();
    }

    public MasterUserPaths getUserPaths()
    {
        File pulseHome = getDataDirectory();
        if (pulseHome != null)
        {
            return new Data(pulseHome);
        }
        return null;
    }

    public File getDataDirectory()
    {
        return getDataConfig().getDataDirectory();
    }

    public File getHomeDirectory()
    {
        if (System.getProperties().containsKey(PULSE_HOME))
        {
            return new File(System.getProperty(PULSE_HOME));
        }
        // this is expected in a dev environment.
        return null;
    }

    public Data getData()
    {
        return getDataConfig().getData();
    }

    private DataConfiguration getDataConfig()
    {
        if (dataConfig == null)
        {
            dataConfig = new DataConfiguration();
            dataConfig.setConfigurationManager(this);
        }
        return dataConfig;
    }

    public void setPulseData(File f)
    {
        DataConfiguration dataConfig = getDataConfig();
        dataConfig.setDataDirectory(f);
    }

}
