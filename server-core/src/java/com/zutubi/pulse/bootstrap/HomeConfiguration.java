package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.Config;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.bootstrap.conf.PropertiesConfig;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;

/**
 * <class-comment/>
 */
public class HomeConfiguration implements HomeResolver
{
    private static final Logger LOG = Logger.getLogger(HomeConfiguration.class);

    private static final String FILENAME = "pulse-init.properties";

    private Config systemProps = null;
    private Config initProps = null;

    private SystemPaths paths = null;

    /**
     * The home property.
     */
    private static final String HOME_PROPERTY_NAME = "pulse.home";

    public Home getHome()
    {
        File dir = getHomeDirectory();
        if (dir != null)
        {
            return new Home(dir);
        }
        return null;
    }

    public void setHomeDirectory(File f)
    {
        Config init = getInitProps();
        init.setProperty(HOME_PROPERTY_NAME, f.getAbsolutePath());
    }

    public File getHomeDirectory()
    {
        Config sys = getSystemProps();
        if (sys.hasProperty(HOME_PROPERTY_NAME))
        {
            return new File(sys.getProperty(HOME_PROPERTY_NAME));
        }
        Config init = getInitProps();
        if (init.hasProperty(HOME_PROPERTY_NAME))
        {
            return new File(init.getProperty(HOME_PROPERTY_NAME));
        }
        return null;
    }

    private Config getSystemProps()
    {
        if (systemProps == null)
        {
            systemProps = new PropertiesConfig(System.getProperties());
        }
        return systemProps;
    }

    private Config getInitProps()
    {
        if (initProps == null)
        {
            initProps = new FileConfig(new File(paths.getConfigRoot(), FILENAME));
        }
        return initProps;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        setSystemPaths(configurationManager.getSystemPaths());
    }

    /**
     * Required resource.
     *
     * @param paths
     */
    public void setSystemPaths(SystemPaths paths)
    {
        this.paths = paths;
    }
}
