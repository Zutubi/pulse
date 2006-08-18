package com.zutubi.pulse.logging;

import com.zutubi.pulse.util.Constants;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * <class-comment/>
 */
public class EventLogFormatter extends Formatter
{
    Date date = new Date();
    private final static String format = "{0,date} {0,time}";
    private MessageFormat formatter;

    private Object args[] = new Object[1];

    public String format(LogRecord record)
    {
        StringBuffer sb = new StringBuffer();

        // Minimize memory allocations here.
        date.setTime(record.getMillis());
        args[0] = date;
        StringBuffer text = new StringBuffer();

        if (formatter == null) {
            formatter = new MessageFormat(format);
        }

        formatter.format(args, text, null);
        sb.append(text);
        sb.append(": ");
        String message = formatMessage(record);
        sb.append(message);
        sb.append(Constants.LINE_SEPARATOR);
        return sb.toString();
    }
}
