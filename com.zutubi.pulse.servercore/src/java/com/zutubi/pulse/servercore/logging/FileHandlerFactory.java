package com.zutubi.pulse.servercore.logging;

import com.zutubi.pulse.core.logging.HandlerFactory;
import com.zutubi.pulse.core.logging.LogUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.XMLFormatter;

/**
 * <class-comment/>
 */
public class FileHandlerFactory implements HandlerFactory
{
    private ObjectFactory objectFactory;

    public FileHandler createHandler(String name, Properties config)
    {
        try
        {
            FileHandler handler = objectFactory.buildBean(FileHandler.class);
            handler.setPattern(LogUtils.getString(config, name + ".pattern", "%h/java%u.log"));
            handler.setLimit(LogUtils.getInt(config, name + ".limit", 0));
            handler.setCount(LogUtils.getInt(config, name + ".count", 1));
            handler.setAppend(LogUtils.getBoolean(config, name + ".append", false));
            handler.setLevel(LogUtils.getLevel(config, name + ".level", Level.ALL));
            handler.setFilter(LogUtils.getFilter(config, name + ".filter", null));
            handler.setFormatter(LogUtils.getFormatter(config, name + ".formatter", new XMLFormatter()));
            handler.setEncoding(LogUtils.getString(config, name + ".encoding", null));
            return handler;
        }
        catch (Exception e)
        {
            System.err.println("Failed to create file handler: Cause: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
