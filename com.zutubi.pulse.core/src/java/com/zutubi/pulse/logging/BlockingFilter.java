package com.zutubi.pulse.logging;

import java.util.logging.LogRecord;
import java.util.logging.Filter;

/**
 * <class-comment/>
 */
public class BlockingFilter implements Filter
{
    public boolean isLoggable(LogRecord record)
    {
        return false;
    }
}
