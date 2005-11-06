package com.cinnamonbob.bootstrap;

import com.cinnamonbob.core.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        if (paths == null)
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
                userProperties.putAll(IOUtils.read(propertiesFile));
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
    
    public String lookupProperty(String key)
    {
        if (System.getProperties().containsKey(key))
        {
            return System.getProperty(key);
        }
        else if (userProperties.containsKey(key))
        {
            return userProperties.getProperty(key);
        }
        return defaultProperties.getProperty(key);
    }

    public boolean hasProperty(String key)
    {
        return System.getProperties().contains(key) || 
                userProperties.containsKey(key) || 
                defaultProperties.containsKey(key);
    }
    
    /**
     * 
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
