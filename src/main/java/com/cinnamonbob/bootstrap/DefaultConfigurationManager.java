package com.cinnamonbob.bootstrap;

import com.cinnamonbob.util.IOHelper;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 
 *
 */
public class DefaultConfigurationManager implements ConfigurationManager
{
    private static final String PROPERTIES_FILENAME = "bob.properties";

    private static final Logger LOG = Logger.getLogger(DefaultConfigurationManager.class.getName());

    private ApplicationPaths paths;

    private Properties defaultProperties = null;
    private Properties userProperties = null;
    
    private File bobRoot;
    
    public void init() throws StartupException
    {
        String bobHome = System.getProperty("bob.home");
        if (bobHome == null || bobHome.length() == 0)
        {
            // fatal error, BOB_HOME property needs to exist.
            throw new StartupException();
        }

        bobRoot = new File(bobHome);
        if (!bobRoot.exists() || !bobRoot.isDirectory())
        {
            // fatal error, BOB_HOME property needs to reference bobs home directory
            throw new StartupException();
        }

        if (paths == null)
        {
            // initialise applicationPaths based on bob.home.        
            paths = new DefaultApplicationPaths(bobRoot);
        }
        initConfig();
    }

    private void initConfig()
    {
        // init user properties.
        File configs = getApplicationPaths().getConfigRoot();
        File propertiesFile = new File(configs, PROPERTIES_FILENAME);
        userProperties = new Properties();
        if (propertiesFile.exists())
        {
            try
            {
                userProperties.putAll(IOHelper.read(propertiesFile));
            } catch (IOException e)
            {
                LOG.log(Level.SEVERE, "Failed to load "+PROPERTIES_FILENAME+".", e);
            }
        }
        
        if (defaultProperties == null)
        {
            defaultProperties = new Properties();
        }
    }
    
    public Config getAppConfig()
    {

        return new Config()
        {
            public int getServerPort()
            {
                if (hasProperty("webapp.port"))
                {
                    return Integer.parseInt(lookupProperty("webapp.port"));
                }
                return 8080;
            }

            public int getAdminPort()
            {
                if (hasProperty("admin.port"))
                {
                    return Integer.parseInt(lookupProperty("admin.port"));
                }
                return 8081;
            }

            public File getProjectRoot()
            {
                if (hasProperty("project.root"))
                {
                    return new File(lookupProperty("project.root"));
                }
                return new File(getApplicationPaths().getUserConfigRoot(), "projects");
            }
        };
    }

    public void setDefaultProperties(Properties properties)
    {
        this.defaultProperties = properties;
    }
    
    private String lookupProperty(String key)
    {
        if (System.getProperties().contains(key))
        {
            return System.getProperty(key);
        }
        else if (userProperties.contains(key))
        {
            return userProperties.getProperty(key);
        }
        return defaultProperties.getProperty(key);
    }

    private boolean hasProperty(String key)
    {
        return System.getProperties().contains(key) || 
                userProperties.contains(key) || 
                defaultProperties.contains(key);
    }
    
    /**
     * 
     * @return
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
