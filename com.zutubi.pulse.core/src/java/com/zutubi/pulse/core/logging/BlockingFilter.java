package com.zutubi.pulse.core.logging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

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
