package com.cinnamonbob.core.util;

import java.text.DateFormat;
import java.util.Date;

/**
 * A time stamp pair to mark the start and end times for something.
 */
public class TimeStamps
{
    public static final long UNINITIALISED_TIME = -1;

    private long queueTime;
    private long startTime;
    private long endTime;

    public TimeStamps()
    {
        queueTime = UNINITIALISED_TIME;
        startTime = UNINITIALISED_TIME;
        endTime = UNINITIALISED_TIME;
    }

    public TimeStamps(long queueTime, long startTime, long endTime)
    {
        this.queueTime = queueTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TimeStamps(TimeStamps other)
    {
        this.queueTime = other.queueTime;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
    }

    public void end()
    {
        endTime = System.currentTimeMillis();
    }

    public long getQueueTime()
    {
        return queueTime;
    }

    public void setQueueTime(long queueTime)
    {
        this.queueTime = queueTime;
    }

    /**
     * @return Returns the startTime.
     */
    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long t)
    {
        startTime = t;
    }

    /**
     * @return Returns the endTime.
     */
    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long t)
    {
        endTime = t;
    }

    public long getElapsed()
    {
        if (!started())
        {
            return UNINITIALISED_TIME;
        }
        else if (ended())
        {
            return endTime - startTime;
        }
        else
        {
            return System.currentTimeMillis() - startTime;
        }
    }

    public String getPrettyQueueTime()
    {
        return getPrettyTime(queueTime);
    }

    public String getPrettyStartTime()
    {
        return getPrettyTime(startTime);
    }

    public String getPrettyEndTime()
    {
        return getPrettyTime(endTime);
    }

    public String getPrettyElapsed()
    {
        StringBuffer result = new StringBuffer();
        long elapsed = getElapsed();

        if (elapsed == UNINITIALISED_TIME)
        {
            return "n/a";
        }

        elapsed = addElapsedPart(elapsed, Constants.HOUR, "hour", result);
        elapsed = addElapsedPart(elapsed, Constants.MINUTE, "minute", result);

        if (elapsed < Constants.SECOND && result.length() == 0)
        {
            result.append("< 1 second");
        }
        else
        {
            addElapsedPart(elapsed, Constants.SECOND, "second", result);
        }

        return result.toString();
    }

    public boolean equals(Object other)
    {
        if (!(other instanceof TimeStamps))
        {
            return false;
        }

        TimeStamps otherStamps = (TimeStamps) other;
        return queueTime == otherStamps.queueTime && startTime == otherStamps.startTime && endTime == otherStamps.endTime;
    }

    private long addElapsedPart(long elapsed, long millisPerPart, String partName, StringBuffer result)
    {
        if (elapsed >= millisPerPart)
        {
            long part = elapsed / millisPerPart;

            if (result.length() > 0)
            {
                result.append(", ");
            }

            result.append(Long.toString(part));
            result.append(' ');
            result.append(partName);

            if (part > 1)
            {
                result.append('s');
            }

            elapsed %= millisPerPart;
        }

        return elapsed;
    }

    public static String getPrettyTime(long time)
    {
        if (time == UNINITIALISED_TIME)
        {
            return "n/a";
        }
        else
        {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date(time));
        }
    }

    public boolean started()
    {
        return startTime != UNINITIALISED_TIME;
    }

    public boolean ended()
    {
        return endTime != UNINITIALISED_TIME;
    }
}
