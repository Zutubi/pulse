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

package com.zutubi.pulse.servercore.util.logging;

import com.google.common.base.Objects;
import com.zutubi.util.adt.CircularBuffer;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A custom logging handler that remembers recent messages in a memory
 * buffer, so they can be viewed via the web interface.
 *
 * On initialisation, this handler registers itself with the logging system
 * and begins storing log messages.
 */
public class ServerMessagesHandler extends Handler
{
    private static final int DEFAULT_CAPACITY = 100;

    private final CircularBuffer<CustomLogRecord> records = new CircularBuffer<CustomLogRecord>(DEFAULT_CAPACITY);

    public void init()
    {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(this);
    }

    public void publish(LogRecord record)
    {
        if(record.getLevel().intValue() >= Level.WARNING.intValue())
        {
            synchronized(records)
            {
                if(records.getCount() > 0)
                {
                    CustomLogRecord previous = records.getElement(records.getCount() - 1);
                    if(Objects.equal(previous.getMessage(), record.getMessage()) &&
                            Objects.equal(previous.getStackTrace(), CustomLogRecord.getStackTrace(record)))
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