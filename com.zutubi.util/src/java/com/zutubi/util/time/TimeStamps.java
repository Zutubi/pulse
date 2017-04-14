/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.util.time;

import com.zutubi.util.Constants;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A time stamp pair to mark the start and end times for something.
 */
public class TimeStamps implements Serializable
{
    public static final long UNINITIALISED_TIME = -1;

    private long queueTime;
    private long startTime;
    private long endTime;
    private long estimatedRunningTime = UNINITIALISED_TIME;

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

    public TimeStamps(long queueTime, long startTime, long endTime, long estimatedRunningTime)
    {
        this.queueTime = queueTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.estimatedRunningTime = estimatedRunningTime;
    }

    public TimeStamps(TimeStamps other)
    {
        this.queueTime = other.queueTime;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.estimatedRunningTime = other.estimatedRunningTime;
    }

    public void end()
    {
        endTime = System.currentTimeMillis();
    }

    public void start()
    {
        startTime = System.currentTimeMillis();
    }

    public boolean hasQueueTime()
    {
        return queueTime != UNINITIALISED_TIME;
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

    private boolean hasEndTime()
    {
        return endTime != UNINITIALISED_TIME;
    }

    public long getEstimatedEndTime()
    {
        return startTime + estimatedRunningTime;
    }

    public boolean hasEstimatedEndTime()
    {
        return estimatedRunningTime != UNINITIALISED_TIME;
    }

    public boolean hasEstimatedTimeRemaining()
    {
        return hasEstimatedEndTime() && !hasEndTime();
    }

    public long getEstimatedTimeRemaining()
    {
        long currentTime = System.currentTimeMillis();
        long estimatedEndTime = getEstimatedEndTime();
        if(currentTime < estimatedEndTime)
        {
            return estimatedEndTime - currentTime;
        }
        else
        {
            return 0;
        }
    }

    public String getPrettyEstimatedTimeRemaining()
    {
        if(hasEstimatedTimeRemaining())
        {

            return getPrettyElapsed(getEstimatedTimeRemaining());
        }
        else
        {
            return "n/a";
        }
    }

    public int getEstimatedPercentComplete()
    {
        long remaining = getEstimatedTimeRemaining();
        if(hasEstimatedTimeRemaining() && remaining > 0)
        {
            double percentRemaining = 100.0 * remaining / estimatedRunningTime;
            return (int)(100.0 - percentRemaining);
        }
        else
        {
            return 100;
        }
    }

    public void setEndTime(long t)
    {
        endTime = t;
    }

    public long getEstimatedRunningTime()
    {
        return estimatedRunningTime;
    }

    public void setEstimatedRunningTime(long estimatedRunningTime)
    {
        this.estimatedRunningTime = estimatedRunningTime;
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

    public String getPrettyQueueDate(Locale locale)
    {
        return getPrettyDate(queueTime, locale);
    }

    public String getPrettyStartDate()
    {
        return getPrettyStartDate(Locale.getDefault());
    }

    public String getPrettyStartDate(Locale locale)
    {
        return getPrettyDate(startTime, locale);
    }

    public String getPrettyEndDate()
    {
        return getPrettyEndDate(Locale.getDefault());
    }

    public String getPrettyEndDate(Locale locale)
    {
        return getPrettyDate(endTime, locale);
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
                result.append(elapsed);
                result.append(" ms");
            }
            else
            {
                addElapsedPart(elapsed, Constants.SECOND, "sec", result);
            }
        }

        return result.toString();
    }

    public static String getPrettyEstimated(long estimated)
    {
        // what is the scale we are dealing with?
        // < 1 minute
        if (estimated < Constants.MINUTE)
        {
            return "< 1 minute";
        }
        if (estimated == Constants.MINUTE)
        {
            return "About 1 minute";
        }
        // About 2 minutes
        // About 3 minutes
        // ...
        // ...
        // About 50 minsutes
        if (Constants.MINUTE < estimated && estimated < 55 * Constants.MINUTE)
        {
            long minutes = (estimated / Constants.MINUTE);
            if (minutes == 1)
            {
                return "About 1 minute";
            }
            return "About " + minutes + " minutes";
        }
        // < 1 hour
        if (estimated < Constants.HOUR)
        {
            return "< 1 hour";
        }
        if (estimated == Constants.HOUR)
        {
            return "About 1 hour";
        }
        // About 2 hours
        // ...
        // ...
        // About 23 hours
        if (Constants.HOUR < estimated && estimated < 23 * Constants.HOUR)
        {
            long hours = (estimated / Constants.HOUR);
            if (hours == 1)
            {
                return "About 1 hour";
            }
            return "About " + hours + " hours";
        }
        // < 1 day
        if (estimated < Constants.DAY)
        {
            return "< 1 day";
        }
        if (estimated == Constants.DAY)
        {
            return "About 1 day";
        }
        // ...
        // ...

        return "A very long time";
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

    public static String getPrettyDate(long time, Locale locale)
    {
        if (time == UNINITIALISED_TIME)
        {
            return "n/a";
        }
        else
        {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, locale).format(new Date(time));
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
