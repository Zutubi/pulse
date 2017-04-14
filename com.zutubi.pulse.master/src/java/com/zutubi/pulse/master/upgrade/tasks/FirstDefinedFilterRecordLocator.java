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

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.HashMap;
import java.util.Map;

/**
 * A record locator that filters records found by another locator, only passing
 * records that are not inherited.  That is, records that are the first
 * definition of a path in a hierarchy.
 */
class FirstDefinedFilterRecordLocator implements RecordLocator
{
    private RecordLocator delegate;
    private TemplatedScopeDetails scope;

    /**
     * @param delegate  delegate locator used to find records to filter
     * @param scope     the scope in which the delegate is locating records
     */
    public FirstDefinedFilterRecordLocator(RecordLocator delegate, TemplatedScopeDetails scope)
    {
        this.delegate = delegate;
        this.scope = scope;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Map<String, Record> allRecords = delegate.locate(recordManager);
        Map<String, Record> result = new HashMap<String, Record>();
        for (Map.Entry<String, Record> entry: allRecords.entrySet())
        {
            if (!scope.hasAncestor(entry.getKey()))
            {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}