package com.zutubi.pulse.logging;

import com.zutubi.util.CircularBuffer;
import com.zutubi.util.StringUtils;

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
                    if(StringUtils.equals(previous.getMessage(), record.getMessage()) &&
                       StringUtils.equals(previous.getStackTrace(), CustomLogRecord.getStackTrace(record)))
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
}