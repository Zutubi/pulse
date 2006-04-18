/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
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
public class SimpleConfigurationManager implements ConfigurationManager
{
    public static final String PULSE_INSTALL = "pulse.install";

    private static final Logger LOG = Logger.getLogger(SimpleConfigurationManager.class);

    private SystemPaths systemPaths = null;
    private HomeConfiguration homeConfig;

    public ApplicationConfiguration getAppConfig()
    {
        UserPaths paths = getUserPaths();
        if (paths != null)
        {
            Config user = new FileConfig(new File(paths.getUserConfigRoot(), "pulse.properties"));
            Config defaults = new FileConfig(new File(getSystemPaths().getConfigRoot(), "pulse-defaults.properties"));
            Config composite = new CompositeConfig(user, new ReadOnlyConfig(defaults));
            return new ApplicationConfigurationSupport(composite);
        }
        else
        {
            Config defaults = new FileConfig(new File(getSystemPaths().getConfigRoot(), "pulse-defaults.properties"));
            return new ApplicationConfigurationSupport(new ReadOnlyConfig(defaults));
        }
    }

    public UserPaths getUserPaths()
    {
        File pulseHome = getHomeDirectory();
        if (pulseHome != null)
        {
            return new Home(pulseHome);
        }
        return null;
    }

    public File getHomeDirectory()
    {
        return getHomeConfig().getHomeDirectory();
    }

    public File getInstallDirectory()
    {
        if (System.getProperties().containsKey(PULSE_INSTALL))
        {
            return new File(System.getProperty(PULSE_INSTALL));
        }
        // this is expected in a dev environment.
        return null;
    }

    public Home getHome()
    {
        return getHomeConfig().getHome();
    }

    public boolean requiresSetup()
    {
        // At the moment, this is nice and simple. Do we have a home configured?
        // and if so, is it valid.
        Home home = getHome();
        if (home == null)
        {
            return true;
        }
        if (!home.isInitialised())
        {
            return true;
        }

        return false;

        // In future, we may want to add further checks here to handle cases where
        // the setup process has failed. ie:
        // a) ensure that the database has been configured - checking a simple property in
        //    the config will do.
        // b) that an admin user exists...
        // c) .. whatever else comes up...
    }

    private HomeConfiguration getHomeConfig()
    {
        if (homeConfig == null)
        {
            homeConfig = new HomeConfiguration();
            homeConfig.setConfigurationManager(this);
        }
        return homeConfig;
    }

    public void setPulseHome(File f)
    {
        HomeConfiguration homeConfig = getHomeConfig();
        homeConfig.setHomeDirectory(f);
    }

    public SystemPaths getSystemPaths()
    {
        if (systemPaths == null)
        {
            String pulseInstall = System.getProperty(PULSE_INSTALL);
            if (pulseInstall == null || pulseInstall.length() == 0)
            {
                // fatal error, PULSE_INSTALL property needs to exist.
                throw new StartupException("Required property '" + PULSE_INSTALL + "' is not set");
            }

            File pulseRoot = new File(pulseInstall);
            if (!pulseRoot.exists() || !pulseRoot.isDirectory())
            {
                // fatal error, PULSE_INSTALL property needs to reference pulse's home directory
                throw new StartupException("Property '" + PULSE_INSTALL + "' does not refer to a " +
                        "directory ('" + pulseInstall + ")");
            }
            // initialise applicationPaths based on pulse.install.
            systemPaths = new DefaultSystemPaths(pulseRoot);
        }
        return systemPaths;
    }

    public void setSystemPaths(SystemPaths paths)
    {
        this.systemPaths = paths;
    }
}
