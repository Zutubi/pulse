package com.cinnamonbob.core;

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
    
    
    TimeStamps(long startTime, long endTime)
    {
        this.startTime = startTime;
        this.endTime = endTime;
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
    
    
    public long getElapsed()
    {
        return endTime - startTime;
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
