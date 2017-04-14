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

import com.google.common.base.Predicate;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.HashMap;
import java.util.Map;

/**
 * A record locator that filters records found by another locator by a given
 * predicate
 */
class PredicateFilterRecordLocator implements RecordLocator
{
    private RecordLocator delegate;
    private Predicate<Record> predicate;

    /**
     * @param delegate  delegate locator used to find records to filter
     * @param predicate defines which records this filter will allow to pass
     */
    public PredicateFilterRecordLocator(RecordLocator delegate, Predicate<Record> predicate)
    {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Map<String, Record> allRecords = delegate.locate(recordManager);
        Map<String, Record> result = new HashMap<String, Record>();
        for (Map.Entry<String, Record> entry: allRecords.entrySet())
        {
            if (predicate.apply(entry.getValue()))
            {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}