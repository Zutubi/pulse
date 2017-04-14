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

package com.zutubi.tove.type.record.events;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Indicates a record has just been updated.
 */
public class RecordUpdatedEvent extends RecordEvent
{
    private Record originalRecord;
    private Record newRecord;

    /**
     * Create a new record updated event.
     *
     * @param source         the source that is raising the event
     * @param path           path of the updated record
     * @param originalRecord the original record values
     * @param newRecord      the new record values
     */
    public RecordUpdatedEvent(RecordManager source, String path, Record originalRecord, Record newRecord)
    {
        super(source, path);
        this.originalRecord = originalRecord;
        this.newRecord = newRecord;
    }

    /**
     * Returns the original record values.
     *
     * @return the original record values
     */
    public Record getOriginalRecord()
    {
        return originalRecord;
    }

    /**
     * Returns the new record values.
     *
     * @return the updated record values
     */
    public Record getNewRecord()
    {
        return newRecord;
    }

    @Override
    public String toString()
    {
        return "Record Updated Event: " + path;
    }
}