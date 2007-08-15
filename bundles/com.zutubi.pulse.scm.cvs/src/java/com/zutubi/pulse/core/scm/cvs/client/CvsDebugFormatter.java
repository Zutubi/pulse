package com.zutubi.pulse.core.scm.cvs.client;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * <class comment/>
 */
public class CvsDebugFormatter extends Formatter
{
    public static ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    public String format(LogRecord record)
    {
        String msg = record.getMessage();

        Object o = contextHolder.get();
        if (o != null && msg.endsWith("\n"))
        {
            msg = String.format("%s%s: ", msg, o);
        }
        return msg;
    }
}
