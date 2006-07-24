package com.zutubi.pulse.logging;

import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Filter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * The Log Configuration Manager handles the systems logging configuration.
 *
 */
public class LogConfigurationManager
{
    private File logConfigDir;

    /**
     *
     */
    private LogConfiguration logConfig;

    public void init()
    {
        updateConfiguration(logConfig.getLoggingLevel());
    }

    public List<String> getAvailableConfigurations()
    {
        // look in the configDirectory subdirectory for all properties files.
        String[] filenames = logConfigDir.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith("logging") && name.endsWith(".properties");
            }
        });

        List<String> configs = new LinkedList<String>();
        for (String filename : filenames)
        {
            configs.add(filename.substring(8, filename.lastIndexOf(".")));
        }
        return configs;
    }

    // what we need is the configuration directory - can we get this directly out of sping?
    // if so, that would be sweet. no need to tie in the ConfigurationManager interface.

    public void updateConfiguration(String config)
    {
        // load requested file.
        File configFile = new File(logConfigDir, "logging." + config + ".properties");
        if (!configFile.exists() || !configFile.canRead())
        {
            throw new IllegalArgumentException("invalid logging configuration '"+config+"'");
        }

        InputStream in = null;
        try
        {
            in = new FileInputStream(configFile);
            LogManager.getLogManager().readConfiguration(in);
            IOUtils.close(in);
            in = new FileInputStream(configFile);

            // the loaded configurations are only applied to pre-existing loggers. Therefore,
            // if we want 'virtual' .level configurations to work, we need to ensure that those
            // loggers exist.
            Properties props = new Properties();
            props.load(in);
            Enumeration propertyNames = props.propertyNames();
            while (propertyNames.hasMoreElements())
            {
                String propertyName = (String) propertyNames.nextElement();
                if (propertyName.endsWith(".level"))
                {
                    // warm up the logger.
                    Logger.getLogger(propertyName.substring(0, propertyName.length() - 6));
                }
            }

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

    public void setSystemPaths(SystemPaths paths)
    {
        File configDirectory = paths.getConfigRoot();
        logConfigDir = new File(configDirectory, "logging");
    }

    /**
     * Required resource.
     *
     * @param config
     */
    public void setLogConfiguration(LogConfiguration config)
    {
        this.logConfig = config;
    }

    static class BlockingFilter implements Filter
    {
        public boolean isLoggable(LogRecord record)
        {
            return false;
        }
    }
}
