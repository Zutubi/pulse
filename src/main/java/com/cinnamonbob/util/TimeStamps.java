package com.cinnamonbob.util;

import java.text.DateFormat;
import java.util.Date;

/**
 * A time stamp pair to mark the start and end times for something.
 */
public class TimeStamps
{
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    private static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

    private long startTime;
    private long endTime;
    
    
    public TimeStamps()
    {
        startTime = System.currentTimeMillis();
        endTime = -1;
    }
    
    
    public TimeStamps(long startTime, long endTime)
    {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    
    public TimeStamps(TimeStamps other)
    {
        this.startTime = other.startTime;
        this.endTime = other.endTime;
    }

    
    public void end()
    {
        endTime = System.currentTimeMillis();
    }

    /**
     * @return Returns the endTime.
     */
    public long getEndTime()
    {
        return endTime;
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
    
    public void setEndTime(long t)
    {
        endTime = t;
    }
    
    public long getElapsed()
    {
        if(endTime > 0)
        {
            return endTime - startTime;
        }
        else
        {
            return System.currentTimeMillis() - startTime;
        }
    }
    
    
    public String getPrettyEndTime()
    {
        return getPrettyTime(endTime);
    }


    public String getPrettyStartTime()
    {
        return getPrettyTime(startTime);
    }

    
    public String getPrettyElapsed()
    {
        StringBuffer result = new StringBuffer();
        long elapsed = getElapsed();
        
        elapsed = addElapsedPart(elapsed, MILLIS_PER_HOUR, "hour", result);
        elapsed = addElapsedPart(elapsed, MILLIS_PER_MINUTE, "minute", result);
        
        if(elapsed < MILLIS_PER_SECOND && result.length() == 0)
        {
            result.append("< 1 second");
        }
        else
        {
            elapsed = addElapsedPart(elapsed, MILLIS_PER_SECOND, "second", result);
        }
        
        return result.toString();
    }

    
    public boolean equals(Object other)
    {
        if(!(other instanceof TimeStamps))
        {
            return false;
        }
        
        TimeStamps otherStamps = (TimeStamps)other;
        return startTime == otherStamps.startTime && endTime == otherStamps.endTime;
    }
    
    
    private long addElapsedPart(long elapsed, long millisPerPart, String partName, StringBuffer result)
    {
        if(elapsed >= millisPerPart)
        {
            long part = elapsed / millisPerPart;
            
            if(result.length() > 0)
            {
                result.append(", ");
            }
            
            result.append(Long.toString(part));
            result.append(' ');
            result.append(partName);
            
            if(part > 1)
            {
                result.append('s');
            }
            
            elapsed %= millisPerPart;
        }
        
        return elapsed;
    }
    
    
    private String getPrettyTime(long time)
    {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(time));
    }
}
