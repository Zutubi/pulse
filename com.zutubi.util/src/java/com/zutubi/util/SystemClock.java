package com.zutubi.util;

/**
 * Implementation of {@link Clock} that uses the system clock - i.e. the
 * implementation to use when you want the real time.
 */
public class SystemClock implements Clock
{
    public long getCurrentTimeMillis()
    {
        return System.currentTimeMillis();
    }
}
