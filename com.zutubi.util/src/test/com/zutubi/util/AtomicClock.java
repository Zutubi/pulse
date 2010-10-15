package com.zutubi.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link Clock} that every request for the
 * time results in a unique time.
 */
public class AtomicClock implements Clock
{
    private AtomicLong time = new AtomicLong();

    public long getCurrentTimeMillis()
    {
        return time.getAndIncrement();
    }
}
