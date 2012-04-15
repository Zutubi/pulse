package com.zutubi.util.time;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link Clock} where every request for the
 * time results in a unique constantly increasing time value being
 * returned.
 */
public class AtomicClock implements Clock
{
    private AtomicLong time = new AtomicLong();

    public long getCurrentTimeMillis()
    {
        return time.getAndIncrement();
    }
}
