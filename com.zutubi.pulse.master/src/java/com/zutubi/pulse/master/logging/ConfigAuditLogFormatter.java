package com.zutubi.pulse.master.logging;

import com.zutubi.util.SystemUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Provides a very lightweight formatting for the config audt logs.
 */
public class ConfigAuditLogFormatter extends Formatter
{
    private StringBuilder builder = new StringBuilder();
    private Date date = new Date();

    public synchronized String format(LogRecord record)
    {
        builder.delete(0, builder.length());

        date.setTime(record.getMillis());
        builder.append(DateFormat.getDateTimeInstance().format(date));
        builder.append(": ");
        builder.append(record.getMessage());
        builder.append(SystemUtils.LINE_SEPARATOR);
        return builder.toString();
    }
}
