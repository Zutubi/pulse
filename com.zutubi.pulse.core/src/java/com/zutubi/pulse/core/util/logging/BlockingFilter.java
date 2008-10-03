package com.zutubi.pulse.core.util.logging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * An implementation of the {@link java.util.logging.Filter} interface
 * that ensures all LogRecords are discarded.
 *
 * @see java.util.logging.Filter
 */
public class BlockingFilter implements Filter
{
    public boolean isLoggable(LogRecord record)
    {
        return false;
    }
}
