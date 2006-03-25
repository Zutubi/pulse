package com.cinnamonbob.bootstrap;

import com.cinnamonbob.bootstrap.conf.CompositeConfig;
import com.cinnamonbob.bootstrap.conf.Config;
import com.cinnamonbob.bootstrap.conf.FileConfig;
import com.cinnamonbob.bootstrap.conf.ReadOnlyConfig;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;

/**
 * <class-comment/>
 */
public class SimpleConfigurationManager implements ConfigurationManager
{
    public static final String BOB_INSTALL = "bob.install";

    private static final Logger LOG = Logger.getLogger(SimpleConfigurationManager.class);

    private SystemPaths systemPaths = null;
    private HomeConfiguration homeConfig;

    public ApplicationConfiguration getAppConfig()
    {
        UserPaths paths = getUserPaths();
        if (paths != null)
        {
            Config user = new FileConfig(new File(paths.getUserConfigRoot(), "bob.properties"));
            Config defaults = new FileConfig(new File(getSystemPaths().getConfigRoot(), "bob-defaults.properties"));
            Config composite = new CompositeConfig(user, new ReadOnlyConfig(defaults));
            return new ApplicationConfigurationSupport(composite);
        }
        else
        {
            Config defaults = new FileConfig(new File(getSystemPaths().getConfigRoot(), "bob-defaults.properties"));
            return new ApplicationConfigurationSupport(new ReadOnlyConfig(defaults));
        }
    }

    public UserPaths getUserPaths()
    {
        File bobHome = getBobHome();
        if (bobHome != null)
        {
            return new Home(bobHome);
        }
        return null;
    }

    public File getBobHome()
    {
        return getHomeConfig().getHomeDirectory();
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

    public void setBobHome(File f)
    {
        HomeConfiguration homeConfig = getHomeConfig();
        homeConfig.setHomeDirectory(f);
        if (!homeConfig.getHome().isInitialised())
        {
            getHomeConfig().getHome().init();
        }
    }

    public SystemPaths getSystemPaths()
    {
        if (systemPaths == null)
        {
            String bobInstall = System.getProperty(BOB_INSTALL);
            if (bobInstall == null || bobInstall.length() == 0)
            {
                // fatal error, BOB_INSTALL property needs to exist.
                throw new StartupException("Required property '" + BOB_INSTALL + "' is not set");
            }

            File bobRoot = new File(bobInstall);
            if (!bobRoot.exists() || !bobRoot.isDirectory())
            {
                // fatal error, BOB_INSTALL property needs to reference bobs home directory
                throw new StartupException("Property '" + BOB_INSTALL + "' does not refer to a " +
                        "directory ('" + bobInstall + ")");
            }
            // initialise applicationPaths based on bob.home.
            systemPaths = new DefaultSystemPaths(bobRoot);
        }
        return systemPaths;
    }

    public void setSystemPaths(SystemPaths paths)
    {
        this.systemPaths = paths;
    }
}
