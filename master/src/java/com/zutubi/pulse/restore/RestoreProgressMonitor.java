package com.zutubi.pulse.restore;

import com.zutubi.pulse.util.TimeStamps;

/**
 *
 *
 */
public class RestoreProgressMonitor
{
    private long startTimestamp;
    private long finishTimestamp;

    private boolean successful = true;

    public boolean isStarted()
    {
        return startTimestamp != 0;
    }

    public void start()
    {
        startTimestamp = System.currentTimeMillis();
    }

    public void finish()
    {
        finishTimestamp = System.currentTimeMillis();
    }

    /**
     * Return the elapsed time.
     *
     * @return formatted string representing the elapsed time.
     */
    public String getElaspedTime()
    {
        // if start timestamp is zero, we have not started.
        if (startTimestamp == 0)
        {
            return TimeStamps.getPrettyElapsed(0);
        }

        long elapsedTime;
        // if finish time is zero, then we have not finished.
        if (finishTimestamp == 0)
        {
            elapsedTime = System.currentTimeMillis() - startTimestamp;
        }
        else
        {
            elapsedTime = finishTimestamp - startTimestamp;
        }
        return TimeStamps.getPrettyElapsed(elapsedTime);
    }

    public boolean isSuccessful()
    {
        return successful;
    }
}
