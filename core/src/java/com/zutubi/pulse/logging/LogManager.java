package com.zutubi.pulse.logging;

import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * <class-comment/>
 */
public class LogManager
{
    private Map<String, HandlerFactory> handlerFactories = new HashMap<String, HandlerFactory>();

    public void setFactories(Map<String, HandlerFactory> factories)
    {
        this.handlerFactories = factories;
    }

    public void reset()
    {
        // reset the logging configuration... like completely blank it out.
        java.util.logging.LogManager.getLogManager().reset();
    }

    public void resetLevels()
    {
        Enumeration<String> loggerNames = java.util.logging.LogManager.getLogManager().getLoggerNames();
        while (loggerNames.hasMoreElements())
        {
            String loggerName = loggerNames.nextElement();
            Logger l = java.util.logging.LogManager.getLogManager().getLogger(loggerName);
            l.setLevel(null);
        }
    }

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
}
