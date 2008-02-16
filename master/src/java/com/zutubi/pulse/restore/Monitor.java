package com.zutubi.pulse.restore;

/**
 *
 *
 */
public class Monitor
{
    private static long UNDEFINED = -1;

    private long startTime = UNDEFINED;
    private long endTime = UNDEFINED;

    public void start()
    {
        startTime = System.currentTimeMillis();
    }

    public boolean isStarted()
    {
        return startTime != UNDEFINED;
    }

    public void finish()
    {
        endTime = System.currentTimeMillis();
        if (startTime == UNDEFINED)
        {
            startTime = endTime;
        }
    }

    public boolean isFinished()
    {
        return endTime != UNDEFINED;
    }

    public long getElapsedTime()
    {
        return endTime - startTime;
    }

}
