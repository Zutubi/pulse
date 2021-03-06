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

import com.zutubi.tove.type.record.RecordManager;

/**
 * Indicates a record has just been inserted.
 */
public class RecordInsertedEvent extends RecordEvent
{
    /**
     * Create a new record inserted event.
     *
     * @param source the source that is raising the event
     * @param path   path of the inserted record
     */
    public RecordInsertedEvent(RecordManager source, String path)
    {
        super(source, path);
    }

    @Override
    public String toString()
    {
        return "Record Inserted Event: " + path;
    }
}
