package com.zutubi.pulse.servercore.util.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * <class comment/>
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
