package com.zutubi.pulse.core.util.logging;

import java.util.Properties;
import java.util.logging.Handler;

/**
 * The factory interface implemented by all handler factories.
 */
public interface HandlerFactory
{
    /**
     * Create a new handler instance.
     *
     * @param name the name of the handler.
     * @param config the logging configuration properties
     *
     * @return a new instance of a handler configured via the properties in the
     * properties object.
     */
    Handler createHandler(String name, Properties config);
}
