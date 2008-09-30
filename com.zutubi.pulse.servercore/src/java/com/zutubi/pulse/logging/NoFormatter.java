package com.zutubi.pulse.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * An implementation of the formatter that applies no extra formatting to log messages.
 *
 * @author Daniel Ostermeier
 */
public class NoFormatter extends Formatter
{
    public String format(LogRecord record)
    {
        return record.getMessage();
    }
}
