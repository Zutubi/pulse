package com.zutubi.pulse.logging;

import com.zutubi.pulse.util.logging.Logger;

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
            eventLogger = Logger.getLogger("com.zutubi.pulse.events");
            // events should only go to the event log.
            eventLogger.setUseParentHandlers(false);
        }
        return eventLogger;
    }
}
