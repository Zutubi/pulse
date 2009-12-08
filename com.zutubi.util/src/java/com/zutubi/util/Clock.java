package com.zutubi.util;

/**
 * Abstraction around the current time.  This allows time-dependent classes to
 * be tested by providing a testing implementation of this interface.
 */
public interface Clock
{
    /**
     * Returns the current time in milliseconds since the Unix epoch.
     *
     * @return the current time measured in milliseconds since midnight,
     *         January 1st 1970, UTC.
     */
    long getCurrentTimeMillis();
}
