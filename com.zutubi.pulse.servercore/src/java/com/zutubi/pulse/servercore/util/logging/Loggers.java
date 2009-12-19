package com.zutubi.pulse.servercore.util.logging;

import com.zutubi.util.logging.Logger;

/**
 */
public class Loggers
{
    private static final String NAME_EVENTS = "com.zutubi.pulse.master.events";

    private static Logger eventLogger;

    /**
     * The Pulse event logger.  All logging sent to this logger will end up in the
     * event log file when event logging is active.  By default, this includes all
     * events.
     *
     * @return the logger for the event log file.
     */
    public static synchronized Logger getEventLogger()
    {
        if (eventLogger == null)
        {
            // The logger for the com.zutubi.pulse.master.events package.  Note, that
            // to ensure the events end up in the events log file, the event handler
            // needs to be bound to the same package.
            eventLogger = Logger.getLogger(NAME_EVENTS);

            // events should only go to the event log.
            eventLogger.setUseParentHandlers(false);
        }
        return eventLogger;
    }
}
