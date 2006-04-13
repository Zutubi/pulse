/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.util.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 */
public class LogRecordHandler extends Handler
{
    public void publish(LogRecord record)
    {
        if (!isLoggable(record))
        {
            return;
        }

        // generate system event (particularly system error events) and publish it 
        // via the event manager
    }

    public void flush()
    {
        //noop
    }

    public void close() throws SecurityException
    {
        flush();
    }
}
