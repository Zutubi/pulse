package com.cinnamonbob.logging;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.util.IOUtils;

import java.util.List;
import java.util.LinkedList;
import java.util.logging.LogManager;
import java.io.*;

/**
 * The Log Configuration Manager handles the systems logging configuration.
 */
public class LogConfigurationManager
{
    private File logConfigDir;
    private ConfigurationManager configurationManager;

    public void init()
    {
        updateConfiguration(configurationManager.getAppConfig().getLogConfig());
    }

    public List<String> getAvailableConfigurations()
    {
        // look in the configDirectory subdirectory for all properties files.
        String[] filenames = logConfigDir.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".properties");
            }
        });

        List<String> configs = new LinkedList<String>();
        for (String filename : filenames)
        {
            configs.add(filename.substring(0, filename.lastIndexOf(".")));
        }
        return configs;
    }

    // what we need is the configuration directory - can we get this directly out of sping?
    // if so, that would be sweet. no need to tie in the ConfigurationManager interface.

    public void updateConfiguration(String config)
    {
        // load requested file.
        File configFile = new File(logConfigDir, config + ".properties");
        if (!configFile.exists() || !configFile.canRead())
        {
            throw new IllegalArgumentException("invalid logging configuration '"+config+"'");
        }

        InputStream in = null;
        try
        {
            in = new FileInputStream(configFile);
            LogManager.getLogManager().readConfiguration(in);
        }
        catch (IOException e)
        {
            // failed to update the logging
            e.printStackTrace();
        }
        finally
        {
            IOUtils.close(in);
        }
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
        File configDirectory = this.configurationManager.getSystemPaths().getConfigRoot();
        logConfigDir = new File(configDirectory, "logging");
    }
}
