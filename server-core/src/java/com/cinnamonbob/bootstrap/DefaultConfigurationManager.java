package com.cinnamonbob.bootstrap;

import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 *
 */
public class DefaultConfigurationManager implements ConfigurationManager
{
    private static final String PROPERTIES_FILENAME = "bob.properties";

    private static final Logger LOG = Logger.getLogger(DefaultConfigurationManager.class);

    private ApplicationPaths paths;

    private Properties defaultProperties = null;
    private Properties userProperties = null;

    private ApplicationConfiguration appConfig;

    public void init() throws StartupException
    {
        if (paths == null)
        {
            String bobHome = System.getProperty("bob.home");
            if (bobHome == null || bobHome.length() == 0)
            {
                // fatal error, BOB_HOME property needs to exist.
                throw new StartupException("Required property 'bob.home' is not set");
            }

            File bobRoot = new File(bobHome);
            if (!bobRoot.exists() || !bobRoot.isDirectory())
            {
                // fatal error, BOB_HOME property needs to reference bobs home directory
                throw new StartupException("Value of 'bob.home' does not refer to a directory ('" + bobHome + ")");
            }
            // initialise applicationPaths based on bob.home.        
            paths = new DefaultApplicationPaths(bobRoot);
        }
        initConfig();
    }

    private void initConfig()
    {
        // init user properties.
        File configs = getApplicationPaths().getUserConfigRoot();
        File propertiesFile = new File(configs, PROPERTIES_FILENAME);
        userProperties = new Properties();
        if (propertiesFile.exists())
        {
            try
            {
                userProperties.putAll(IOUtils.read(propertiesFile));
            }
            catch (IOException e)
            {
                LOG.severe("Failed to load " + PROPERTIES_FILENAME + ".", e);
            }
        }

        if (defaultProperties == null)
        {
            defaultProperties = new Properties();
        }
    }

    public ApplicationConfiguration getAppConfig()
    {
        if (appConfig == null)
        {
            Configuration system = new ReadOnlyConfiguration(System.getProperties());
            Configuration users = new ReadOnlyConfiguration(userProperties);
            Configuration defaults = new ReadOnlyConfiguration(defaultProperties);

            appConfig = new ApplicationConfigurationSupport(system, users, defaults);
        }
        return appConfig;
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
