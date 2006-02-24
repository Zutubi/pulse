package com.cinnamonbob.bootstrap;

import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.bootstrap.config.Configuration;
import com.cinnamonbob.bootstrap.config.FileConfiguration;
import com.cinnamonbob.bootstrap.config.CompositeConfiguration;
import com.cinnamonbob.bootstrap.config.ReadOnlyConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class SimpleConfigurationManager implements ConfigurationManager
{
    private static final Logger LOG = Logger.getLogger(SimpleConfigurationManager.class);

    private SystemPaths systemPaths = null;

    public ApplicationConfiguration getAppConfig()
    {
        UserPaths paths = getUserPaths();
        if (paths != null)
        {
            Configuration user = new FileConfiguration(new File(paths.getUserConfigRoot(), "bob.properties"));
            Configuration defaults = new FileConfiguration(new File(getSystemPaths().getConfigRoot(), "bob-defaults.properties"));
            Configuration composite = new CompositeConfiguration(user, new ReadOnlyConfiguration(defaults));
            return new ApplicationConfigurationSupport(composite);
        }
        else
        {
            Configuration defaults = new FileConfiguration(new File(getSystemPaths().getConfigRoot(), "bob-defaults.properties"));
            return new ApplicationConfigurationSupport(new ReadOnlyConfiguration(defaults));
        }
    }

    public UserPaths getUserPaths()
    {
        File bobHome = getBobHome();
        if (bobHome != null)
        {
            return new DefaultUserPaths(bobHome);
        }
        return null;
    }

    public File getBobHome()
    {
        // check system properties
        if (System.getProperties().containsKey("bob.home"))
        {
            return new File(System.getProperty("bob.home"));
        }
        // lookup file.
        File f = new File(getSystemPaths().getConfigRoot(), "bob-init.properties");
        if (!f.exists())
        {
            return null;
        }

        Properties props;
        try
        {
            props = IOUtils.read(f);
        }
        catch (IOException e)
        {
            LOG.severe(e);
            return null;
        }
        if (props.containsKey("bob.home"))
        {
            return new File(props.getProperty("bob.home"));
        }
        return null;
    }

    public void setBobHome(File f)
    {
        Properties props = new Properties();
        props.setProperty("bob.home", f.getAbsolutePath());
        try
        {
            IOUtils.write(props, new File(getSystemPaths().getConfigRoot(), "bob-init.properties"));
        }
        catch (IOException e)
        {
            LOG.severe(e);
        }
    }

    private SystemPaths initSystemPaths()
    {
        String bobInstall = System.getProperty("bob.install");
        if (bobInstall == null || bobInstall.length() == 0)
        {
            // fatal error, BOB_INSTALL property needs to exist.
            throw new StartupException("Required property 'bob.install' is not set");
        }

        File bobRoot = new File(bobInstall);
        if (!bobRoot.exists() || !bobRoot.isDirectory())
        {
            // fatal error, BOB_INSTALL property needs to reference bobs home directory
            throw new StartupException("Property 'bob.install' does not refer to a directory ('" + bobInstall + ")");
        }
        // initialise applicationPaths based on bob.home.
        return new DefaultSystemPaths(bobRoot);
    }

    public SystemPaths getSystemPaths()
    {
        if (systemPaths == null)
        {
            systemPaths = initSystemPaths();
        }
        return systemPaths;
    }

    public void setSystemPaths(SystemPaths paths)
    {
        this.systemPaths = paths;
    }

}
