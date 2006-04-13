/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core.util;

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

    public String getPrettyQueueDate()
    {
        return getPrettyDate(queueTime);
    }

    public String getPrettyStartDate()
    {
        return getPrettyDate(startTime);
    }

    public String getPrettyEndDate()
    {
        return getPrettyDate(endTime);
    }

    public static String getPrettyElapsed(long elapsed)
    {
        return getPrettyElapsed(elapsed, -1);
    }

    public static String getPrettyElapsed(long elapsed, int maxUnits)
    {
        StringBuffer result = new StringBuffer();
        int unitsAdded = 0;
        long newElapsed;

        if (elapsed == UNINITIALISED_TIME)
        {
            return "n/a";
        }

        if(maxUnits < 0 || unitsAdded < maxUnits)
        {
            newElapsed = addElapsedPart(elapsed, Constants.YEAR, "year", result);
            if(newElapsed >= 0)
            {
                elapsed = newElapsed;
                unitsAdded++;
            }
        }

        if(maxUnits < 0 || unitsAdded < maxUnits)
        {
            newElapsed = addElapsedPart(elapsed, Constants.WEEK, "week", result);
            if(newElapsed >= 0)
            {
                elapsed = newElapsed;
                unitsAdded++;
            }
            else if(unitsAdded > 0)
            {
                unitsAdded++;
            }
        }

        if(maxUnits < 0 || unitsAdded < maxUnits)
        {
            newElapsed = addElapsedPart(elapsed, Constants.DAY, "day", result);
            if(newElapsed >= 0)
            {
                elapsed = newElapsed;
                unitsAdded++;
            }
            else if(unitsAdded > 0)
            {
                unitsAdded++;
            }
        }

        if(maxUnits < 0 || unitsAdded < maxUnits)
        {
            newElapsed = addElapsedPart(elapsed, Constants.HOUR, "hour", result);
            if(newElapsed >= 0)
            {
                elapsed = newElapsed;
                unitsAdded++;
            }
            else if(unitsAdded > 0)
            {
                unitsAdded++;
            }
        }

        if(maxUnits < 0 || unitsAdded < maxUnits)
        {
            newElapsed = addElapsedPart(elapsed, Constants.MINUTE, "min", result);
            if(newElapsed >= 0)
            {
                elapsed = newElapsed;
                unitsAdded++;
            }
            else if(unitsAdded > 0)
            {
                unitsAdded++;
            }
        }

        if(maxUnits < 0 || unitsAdded < maxUnits)
        {
            if (elapsed < Constants.SECOND && result.length() == 0)
            {
                result.append("< 1 sec");
            }
            else
            {
                addElapsedPart(elapsed, Constants.SECOND, "sec", result);
            }
        }

        return result.toString();
    }

    public String getPrettyElapsed()
    {
        return getPrettyElapsed(getElapsed());
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

    private static long addElapsedPart(long elapsed, long millisPerPart, String partName, StringBuffer result)
    {
        if (elapsed >= millisPerPart)
        {
            long part = elapsed / millisPerPart;

            if (result.length() > 0)
            {
                result.append(" ");
            }

            result.append(Long.toString(part));
            result.append(' ');
            result.append(partName);

            if (part > 1)
            {
                result.append('s');
            }

            return elapsed % millisPerPart;
        }

        return -1;
    }

    public static String getPrettyTime(long time)
    {
        if (time == UNINITIALISED_TIME)
        {
            return "n/a";
        }
        else
        {
            return getPrettyElapsed(System.currentTimeMillis() - time, 2) + " ago";
        }
    }

    public static String getPrettyDate(long time)
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
