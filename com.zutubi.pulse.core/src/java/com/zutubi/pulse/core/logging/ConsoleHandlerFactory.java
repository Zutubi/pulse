package com.zutubi.pulse.core.logging;

import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

/**
 * <class-comment/>
 */
public class ConsoleHandlerFactory implements HandlerFactory
{
    public ConsoleHandler createHandler(String name, Properties config)
    {
        ConsoleHandler handler = new ConsoleHandler();

        handler.setLevel(LogUtils.getLevel(config, name + ".level", Level.ALL));
        handler.setFilter(LogUtils.getFilter(config, name +".filter", null));
        handler.setFormatter(LogUtils.getFormatter(config, name +".formatter", new SimpleFormatter()));

        return handler;
    }
}
