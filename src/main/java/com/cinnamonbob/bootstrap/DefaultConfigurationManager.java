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

    private BootstrapManager bootstrapManager;

    public void setBootstrapManager(BootstrapManager bootstrapManager)
    {
        this.bootstrapManager = bootstrapManager;
    }

    public Config getAppConfig()
    {
        File configs = bootstrapManager.getApplicationPaths().getConfigRoot();
        File propertiesFile = new File(configs, PROPERTIES_FILENAME);
        final Properties properties = new Properties();
        if (propertiesFile.exists())
        {
            try
            {
                properties.putAll(IOHelper.read(propertiesFile));
            } catch (IOException e)
            {
                LOG.log(Level.SEVERE, "Failed to load "+PROPERTIES_FILENAME+".", e);
            }
        }

        return new Config()
        {
            public int getServerPort()
            {
                if (properties.containsKey("port"))
                {
                    return Integer.parseInt(properties.getProperty("port"));
                }
                return 8080;
            }

            public File getProjectRoot()
            {
                if (properties.contains("project.root"))
                {
                    return new File(properties.getProperty("project.root"));
                }
                
                return new File(bootstrapManager.getApplicationPaths().getUserConfigRoot(), "projects");
            }
        };
    }
}
