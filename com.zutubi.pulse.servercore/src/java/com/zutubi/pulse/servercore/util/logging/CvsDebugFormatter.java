package com.zutubi.pulse.servercore.util.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formatter for cvs debug output.  It applies the current threads id to the start
 * of each debug comment so that simultaneous debugging output from multiple threads
 * can be differentiated.
 */
public class CvsDebugFormatter extends Formatter
{
    public String format(LogRecord record)
    {
        String msg = record.getMessage();

        if (msg.endsWith("\n"))
        {
            msg = String.format("%s: %s", Thread.currentThread().getId(), msg);
        }
        return msg;
    }
}
