package com.zutubi.pulse.servercore.logging;

import com.zutubi.util.logging.Logger;

/**
 * <class-comment/>
 */
public class Loggers
{
    private static Logger eventLogger;

    public static synchronized Logger getEventLogger()
    {
        if (eventLogger == null)
        {
            // the logger for the com.zutubi.pulse.master.events package.
            eventLogger = Logger.getLogger("com.zutubi.pulse.master.events");

            // events should only go to the event log.
            eventLogger.setUseParentHandlers(false);
        }
        return eventLogger;
    }
}
