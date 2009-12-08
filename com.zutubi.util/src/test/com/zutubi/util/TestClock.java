package com.zutubi.util;

/**
 * Implementation of {@link Clock} that allows you to control the time.
 */
public class TestClock implements Clock
{
    private long time;

    public TestClock(long time)
    {
        this.time = time;
    }

    public void add(long duration)
    {
        time += duration;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public long getCurrentTimeMillis()
    {
        return time;
    }
}
