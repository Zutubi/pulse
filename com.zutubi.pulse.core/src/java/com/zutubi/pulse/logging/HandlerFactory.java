package com.zutubi.pulse.logging;

import java.util.Properties;
import java.util.logging.Handler;

/**
 * <class-comment/>
 */
public interface HandlerFactory
{
    Handler createHandler(String name, Properties config);
}
