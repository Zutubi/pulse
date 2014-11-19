package com.zutubi.pulse.core.util.logging;

import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * The LogManager provides a wrapper around the default java.util.logging.LogManager that
 * allows for the use of a custom logging configuration file format.
 *
 * The following is a brief overview of the custom logging format, which is heavily based on
 * the default, with a few additions.
 *
 * config:  used to define one of more configuration classes that can be used to programmatically
 * configure the logging system.  The class names must be fully qualified and any configuration
 * handled in the no argument constructor.
 *
 * handlers:  list of the logging handlers that are defined later in the log configuration file.
 *
 * For example:
 *
 * handlers=eventHandler
 *
 * handler.type: this is a required property for all of the configured handlers, and is used to
 * define the handlers type. Available types include ConsoleHandler, FileHandler and MemoryHandler.
 * The handlers type can also be a fully qualifies class name if you wish to use a custom handler.
 * If you use one of the existing types, you can configure it by handler.xxx properties, where xxx
 * are the properties supported by that specific handler implementation.
 *
 * For example:
 *
 * eventHandler.type=FileHandler
 * eventHandler.pattern=%l/event%u.%g.log
 * eventHandler.limit=200000
 * eventHandler.count=5
 * eventHandler.append=false
 * eventHandler.formatter=com.zutubi.pulse.servercore.util.logging.EventLogFormatter
 * eventHandler.level=ALL
 *
 * xxx.level: this defines the logging level for a specific level in the logging hierarchy.
 *
 * For example:
 *
 * org.springframework.level=WARNING
 * org.hibernate.level=WARNING
 *
 * xxx.handler: this binds the previously defines handlers to a specific level in the logging hierarchy
 *
 * For example:
 *
 * com.zutubi.pulse.master.events.handler=eventHandler
 *
 */
public class LogManager
{
    private Map<String, HandlerFactory> handlerFactories = new HashMap<String, HandlerFactory>();

    /**
     * Reset the existing logging configuration.
     *
     * @see java.util.logging.LogManager#reset() 
     */
    public void reset()
    {
        // reset the logging configuration... like completely blank it out.
        java.util.logging.LogManager.getLogManager().reset();
    }

    /**
     * Reset the logging levels for all of the configured Loggers to null.
     *
     * @see java.util.logging.Logger#setLevel(java.util.logging.Level)  
     */
    public void resetLevels()
    {
        Enumeration<String> loggerNames = java.util.logging.LogManager.getLogManager().getLoggerNames();
        while (loggerNames.hasMoreElements())
        {
            String loggerName = loggerNames.nextElement();
            Logger l = java.util.logging.LogManager.getLogManager().getLogger(loggerName);
            // Note that on OpenJDK platforms, there is no guarantee that a logger will exist.  The logger instance
            // may have been garbage collected.  Hence we need this != null check.
            if (l != null)
            {
                l.setLevel(null);
            }
        }
    }

    /**
     * Configure the logging system via the details contained within the file.  It is expected that
     * the contents of this file can be loaded using the {@link java.util.Properties#load(java.io.InputStream)}
     *
     * @param config the properties file.
     */
    public void configure(File config)
    {
        if (!config.isFile())
        {
            return;
        }

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(config);
            Properties props = new Properties();
            props.load(fis);
            configure(props);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            IOUtils.close(fis);
        }
    }

    public void configure(Properties config)
    {
        List<String> configNames = LogUtils.getList(config, "config", new LinkedList<String>());
        for (String configName : configNames)
        {
            try
            {
                Class clz = ClassLoader.getSystemClassLoader().loadClass(configName);
                clz.newInstance();
            }
            catch (Exception ex)
            {
                System.err.println("Can't load config class \"" + configName + "\"");
                System.err.println("" + ex);
            }
        }

        // load handlers.
        List<String> handlerNames = LogUtils.getList(config, "handlers", new LinkedList<String>());

        // create mapping for handlers.
        Map<String, Handler> handlers = new HashMap<String, Handler>();
        for (String handlerName : handlerNames)
        {
            if (!config.containsKey(handlerName + ".type"))
            {
                continue;
            }
            String type = config.getProperty(handlerName + ".type");

            Handler handler = createHandler(type, handlerName, config);
            if (handler != null)
            {
                handlers.put(handlerName, handler);
            }
        }

        // read the rest of the configuration file, setting up the components as necessary.
        Enumeration<?> propertyNames = config.propertyNames();
        while (propertyNames.hasMoreElements())
        {
            String propertyName = (String) propertyNames.nextElement();
            if (propertyName.compareTo("handlers") == 0)
            {
                continue;
            }

            if (propertyName.endsWith(".level"))
            {
                String name = propertyName.substring(0, propertyName.length() - 6);
                Logger l = Logger.getLogger(name);
                l.setLevel(LogUtils.getLevel(config, propertyName, null));
            }
            else if (propertyName.endsWith(".useParentHandlers"))
            {
                String name = propertyName.substring(0, propertyName.length() - 18);
                Logger l = Logger.getLogger(name);
                l.setUseParentHandlers(LogUtils.getBoolean(config, propertyName, true));
            }
            else if (propertyName.endsWith(".handler"))
            {
                String name = propertyName.substring(0, propertyName.length() - 8);
                Logger l = Logger.getLogger(name);
                List<String> names = LogUtils.getList(config, propertyName, Collections.<String>emptyList());
                for (String n : names)
                {
                    Handler h = handlers.get(n);
                    if (h != null)
                    {
                        l.addHandler(h);
                    }
                }
            }
        }
    }

    private Handler createHandler(String type, String handlerName, Properties config)
    {
        // lookup handler factory.
        if (handlerFactories.containsKey(type))
        {
            HandlerFactory handlerFactory = handlerFactories.get(type);
            return handlerFactory.createHandler(handlerName, config);
        }

        // otherwise, just try to instantiate it
        try
        {
            Class cls = Class.forName(type);
            return (Handler) cls.newInstance();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void setFactories(Map<String, HandlerFactory> factories)
    {
        this.handlerFactories = factories;
    }
}
