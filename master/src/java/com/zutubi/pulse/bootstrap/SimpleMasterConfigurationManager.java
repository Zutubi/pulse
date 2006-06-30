package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.CompositeConfig;
import com.zutubi.pulse.bootstrap.conf.Config;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.bootstrap.conf.ReadOnlyConfig;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;

/**
 * <class-comment/>
 */
public class SimpleMasterConfigurationManager extends AbstractConfigurationManager implements MasterConfigurationManager
{
    private static final Logger LOG = Logger.getLogger(SimpleMasterConfigurationManager.class);

    private DataConfiguration dataConfig;

    public MasterApplicationConfiguration getAppConfig()
    {
        MasterUserPaths paths = getUserPaths();
        if (paths != null)
        {
            Config user = new FileConfig(new File(paths.getUserConfigRoot(), "pulse.properties"));
            Config defaults = new FileConfig(new File(getSystemPaths().getConfigRoot(), "pulse-defaults.properties"));
            Config composite = new CompositeConfig(user, new ReadOnlyConfig(defaults));
            return new MasterApplicationConfigurationSupport(composite);
        }
        else
        {
            Config defaults = new FileConfig(new File(getSystemPaths().getConfigRoot(), "pulse-defaults.properties"));
            return new MasterApplicationConfigurationSupport(new ReadOnlyConfig(defaults));
        }
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
