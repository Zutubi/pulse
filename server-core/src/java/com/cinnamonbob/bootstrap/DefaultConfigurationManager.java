package com.cinnamonbob.bootstrap;

import com.cinnamonbob.bootstrap.config.*;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;
import java.util.Properties;

/**
 * The Configuration manager is the central location for retrieving system Configurations.
 *
 */
public class DefaultConfigurationManager implements ConfigurationManager
{
    private static final Logger LOG = Logger.getLogger(DefaultConfigurationManager.class);

    /**
     * The bob user properties files contains the customised property values as
     * defined by the user. This file is located in the bob.home directory and will
     * persist the users configuration between upgrades.
     *
     * NOTE: There is a chance that the user will manually modify this file. We need
     * to ensure that the behaviour of properties contained within this file under
     * such circumstances are well defined.
     */
    private static final String PROPERTIES_FILENAME = "bob.properties";

    /**
     * This file contains a set of default values for system properties. These values
     * are made available in the case where the user has not specified values.
     */
    private static final String DEFAULTS_FILENAME = "bob-defaults.properties";

    /**
     * A properties file that contains data necessary for running bob. In particular,
     * this file contains the bob.home property.
     *
     * WARNING: This properties file is located in the installation directories, and therefore
     * does not persist during an upgrade.
     */
    private static final String INIT_FILENAME = "bob-init.properties";

    /**
     * This property defines whether or not it is okay to cache the properties handled within
     * this configuration manager. This should be set to true unless you intend to make changes
     * to the various properties files directly.
     */
    private static final boolean CACHE_PROPERTIES = true;

    /**
     * The name of bobs installation directory. This is required at startup so that this
     * configuration manager is able to orient itself and provide other configuration files.
     */
    private static final String BOB_INSTALL = "bob.install";

    private static final String BOB_HOME = InitConfiguration.BOB_HOME;

    private ApplicationPaths paths;

    private Properties defaultProperties = null;

    /**
     * Configuration object for the PROPERTIES_FILE
     */
    private Configuration userConfig;

    /**
     * Configuration object for the DEFAULTS_FILE
     */
    private Configuration defaultConfig;

    /**
     * Configuration object for System.getProperties()
     */
    private Configuration systemConfig;

    /**
     * Configuration object for the INIT_FILE
     */
    private Configuration initConfig;

    public void init() throws StartupException
    {
        if (paths == null)
        {
            String bobInstall = System.getProperty(BOB_INSTALL);
            if (bobInstall == null || bobInstall.length() == 0)
            {
                // fatal error, BOB_INSTALL property needs to exist.
                throw new StartupException("Required property '"+BOB_INSTALL+"' is not set");
            }

            File bobRoot = new File(bobInstall);
            if (!bobRoot.exists() || !bobRoot.isDirectory())
            {
                // fatal error, BOB_INSTALL property needs to reference bobs home directory
                throw new StartupException("Property '"+BOB_INSTALL+"' does not refer to a directory ('" + bobInstall + ")");
            }
            // initialise applicationPaths based on bob.home.        
            paths = new DefaultApplicationPaths(bobRoot);
        }
    }

    public ApplicationConfiguration getAppConfig()
    {
        return new ApplicationConfigurationSupport(new CompositeConfiguration(
                        systemConfig() ,
                        userConfig(),
                        defaultConfig())
        );
    }

    public InitConfiguration getInitConfig()
    {
        return new InitConfigurationSupport(initConfig());
    }

    protected Configuration systemConfig()
    {
        if (systemConfig == null)
        {
            systemConfig = new ReadOnlyConfiguration(new PropertiesConfiguration(System.getProperties()));
        }
        return systemConfig;
    }

    protected Configuration initConfig()
    {
        if (initConfig == null)
        {
            File initFile = new File(paths.getConfigRoot(), INIT_FILENAME);
            initConfig = new FileConfiguration(initFile);
        }
        return initConfig;
    }

    protected Configuration defaultConfig()
    {
        if (defaultConfig == null)
        {
            File defaultsFile = new File(paths.getConfigRoot(), DEFAULTS_FILENAME);
            defaultConfig = new ReadOnlyConfiguration(new FileConfiguration(defaultsFile));
        }
        return defaultConfig;
    }

    protected Configuration userConfig()
    {
        if (userConfig == null)
        {
            if (initConfig().hasProperty(BOB_HOME))
            {
                String bobHome = initConfig().getProperty(BOB_HOME);
                userConfig = new FileConfiguration(new File(bobHome, PROPERTIES_FILENAME));
            }
            else
            {
                // while bob.home has not been defined, this configuration instance will
                // be read only with no user component. When the bob.home directory is available,
                // then we can provide a user editable component and allow the user to customise
                // the defaults.

                // consider using a temporary configuration, and then copying the data across
                // when the actual configuration object becomes available..
                return new PropertiesConfiguration();
            }
        }
        return userConfig;
    }

    public void setDefaultProperties(Properties properties)
    {
        this.defaultProperties = properties;
    }

    /**
     * @return application paths
     */
    public ApplicationPaths getApplicationPaths()
    {
        return paths;
    }

    /**
     * Set the application paths to be used.
     *
     * @param paths
     */
    public void setApplicationPaths(ApplicationPaths paths)
    {
        this.paths = paths;
    }
}
