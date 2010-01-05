package com.zutubi.pulse.servercore.util.logging;

import com.zutubi.util.Constants;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * The formatter that is used to control the formatting of what is
 * written to the systems event log.
 *
 * @see Formatter
 */
public class EventLogFormatter extends Formatter
{
    private final static String FORMAT = "{0,date} {0,time}";

    private Date date = new Date();
    private MessageFormat formatter = new MessageFormat(FORMAT);

    private Object args[] = new Object[1];

    public String format(LogRecord record)
    {
        StringBuilder builder = new StringBuilder();

        // Minimize memory allocations here.
        date.setTime(record.getMillis());
        args[0] = date;

        StringBuffer formatBuffer = new StringBuffer();
        formatter.format(args, formatBuffer, null);
        builder.append(formatBuffer);
        builder.append(": ");
        String message = formatMessage(record);
        builder.append(message);
        builder.append(Constants.LINE_SEPARATOR);
        return builder.toString();
    }
}
