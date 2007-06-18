package com.zutubi.pulse.logging;

import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.events.AllEventListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * The Log Configuration Manager handles the systems logging configuration.
 *
 */
public class LogConfigurationManager
{
    private File logConfigDir;

    private File configDir;

    private LogManager logManager;

    private EventManager eventManager;

    /**
     *
     */
    private LogConfiguration logConfig;

    public void init()
    {
        logManager.reset();

        // load default configuration.
        logManager.configure(new File(configDir, "logging.properties"));

        applyConfig();
        
        // publish all events that pass through the event manager to the event logger
        final Logger evtLogger = Loggers.getEventLogger();
        eventManager.register(new AllEventListener()
        {
            public void handleEvent(Event evt)
            {
                try
                {
                    evtLogger.info(evt.toString());
                }
                catch (Throwable t)
                {
                    // noop.
                    evtLogger.info("Failed to log event. Reason: " + t.getMessage(), t);
                }
            }
        });
    }

    public void applyConfig()
    {
        updateConfiguration(logConfig.getLoggingLevel());
        setEventLoggingEnabled(logConfig.isEventLoggingEnabled());
    }

    private void updateConfiguration(String config)
    {
        // load requested file.
        File configFile = new File(logConfigDir, "logging." + config + ".properties");
        if (!configFile.isFile() || !configFile.canRead())
        {
            return;
        }

        logManager.resetLevels();
        logManager.configure(configFile);
    }

    private void setEventLoggingEnabled(boolean b)
    {
        Logger evtLogger = Loggers.getEventLogger();
        Handler[] handlers = evtLogger.getDelegate().getHandlers();
        Level handlerLevel = (b)? Level.ALL : Level.OFF;
        for (Handler h : handlers)
        {
            h.setLevel(handlerLevel);
        }
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

    public void setSystemPaths(SystemPaths paths)
    {
        configDir = paths.getConfigRoot();
        logConfigDir = new File(configDir, "logging");
    }

    public void setLogConfiguration(LogConfiguration config)
    {
        this.logConfig = config;
    }

    public void setLogManager(LogManager logManager)
    {
        this.logManager = logManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
