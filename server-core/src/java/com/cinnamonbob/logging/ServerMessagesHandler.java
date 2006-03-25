package com.cinnamonbob.logging;

import com.cinnamonbob.util.CircularBuffer;

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

    private CircularBuffer<LogRecord> records = new CircularBuffer<LogRecord>(DEFAULT_CAPACITY);

    public void init()
    {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(this);
    }

    public void publish(LogRecord record)
    {
        if(record.getLevel() == Level.WARNING || record.getLevel() == Level.SEVERE)
        {
            records.append(record);
        }
    }

    public void flush()
    {
    }

    public void close() throws SecurityException
    {
        records.clear();
    }

    public Iterator<LogRecord> iterator()
    {
        return records.takeSnapshot().iterator();
    }

    public List<LogRecord> takeSnapshot()
    {
        return records.takeSnapshot();
    }
}
