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
    public static final String PULSE_HOME = "pulse.home";

    private static final Logger LOG = Logger.getLogger(SimpleConfigurationManager.class);

    private SystemPaths systemPaths = null;
    private DataConfiguration dataConfig;

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

    public boolean requiresSetup()
    {
        // At the moment, this is nice and simple. Do we have a data configured?
        // and if so, is it valid.
        Data data = getData();
        if (data == null)
        {
            return true;
        }
        if (!data.isInitialised())
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

    public SystemPaths getSystemPaths()
    {
        if (systemPaths == null)
        {
            String pulseHome = System.getProperty(PULSE_HOME);
            if (pulseHome == null || pulseHome.length() == 0)
            {
                // fatal error, PULSE_HOME property needs to exist.
                throw new StartupException("Required property '" + PULSE_HOME + "' is not set");
            }

            File pulseRoot = new File(pulseHome);
            if (!pulseRoot.exists() || !pulseRoot.isDirectory())
            {
                // fatal error, PULSE_HOME property needs to reference pulse's home directory
                throw new StartupException("Property '" + PULSE_HOME + "' does not refer to a " +
                        "directory ('" + pulseHome + ")");
            }
            // initialise applicationPaths based on pulse.home.
            systemPaths = new DefaultSystemPaths(pulseRoot);
        }
        return systemPaths;
    }

    public void setSystemPaths(SystemPaths paths)
    {
        this.systemPaths = paths;
    }
}
