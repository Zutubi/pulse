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

package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.collect.ImmutableMap;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Collections;
import java.util.Map;

/**
 * A locator that finds a single record at a specific path, if such a record
 * exists.  If not, no records are returned.
 */
class PathRecordLocator implements RecordLocator
{
    private String path;

    /**
     * @param path path to look up the record from
     * @see com.zutubi.tove.type.record.RecordManager#select(String)
     */
    public PathRecordLocator(String path)
    {
        this.path = path;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Record record = recordManager.select(path);
        if (record == null)
        {
            return Collections.emptyMap();
        }
        else
        {
            return ImmutableMap.of(path, record);
        }
    }
}
