package com.zutubi.pulse.logging;

import java.util.logging.*;
import java.util.Properties;

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
