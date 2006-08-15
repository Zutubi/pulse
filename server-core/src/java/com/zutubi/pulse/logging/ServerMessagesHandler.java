package com.zutubi.pulse.logging;

import com.zutubi.pulse.util.CircularBuffer;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A custom logging handler that remembers recent messages in a memory
 * buffer, so they can be viewed via the web interface.
 */
public class ServerMessagesHandler extends Handler
{
    private static final int DEFAULT_CAPACITY = 100;

    private CircularBuffer<CustomLogRecord> records = new CircularBuffer<CustomLogRecord>(DEFAULT_CAPACITY);

    public void init()
    {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(this);
    }

    public void publish(LogRecord record)
    {
        if(record.getLevel() == Level.WARNING || record.getLevel() == Level.SEVERE)
        {
            synchronized(records)
            {
                if(records.getCount() > 0)
                {
                    CustomLogRecord previous = records.getElement(records.getCount() - 1);
                    if(nullSafeEquals(previous.getMessage(), record.getMessage()) &&
                       nullSafeEquals(previous.getStackTrace(), CustomLogRecord.getStackTrace(record)))
                    {
                        previous.repeated(record);
                        return;
                    }
                }
            }

            records.append(new CustomLogRecord(record));
        }
    }

    public void flush()
    {
    }

    public void close() throws SecurityException
    {
        records.clear();
    }

    public Iterator<CustomLogRecord> iterator()
    {
        return records.takeSnapshot().iterator();
    }

    public List<CustomLogRecord> takeSnapshot()
    {
        return records.takeSnapshot();
    }

    private boolean nullSafeEquals(Throwable t1, Throwable t2)
    {
        if(t1 == null)
        {
            return t2 == null;
        }

        return t1.getClass() == t2.getClass() && nullSafeEquals(t1.getMessage(), t2.getMessage());
    }

    private boolean nullSafeEquals(String s1, String s2)
    {
        if(s1 == null)
        {
            return s2 == null;
        }

        return s1.equals(s2);
    }
}