package com.zutubi.pulse.logging;

import java.util.logging.*;

/**
 * The ConsoleConfig object handles the initialisation of the Console
 * handler.
 *
 */
public class ConsoleConfig
{
    public ConsoleConfig()
    {
        // register the console handler. All initial logging goes to the console.
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(Level.WARNING);

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(consoleHandler);
    }
}
