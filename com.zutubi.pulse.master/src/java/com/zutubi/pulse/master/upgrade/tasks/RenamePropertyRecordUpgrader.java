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

import com.zutubi.tove.type.record.MutableRecord;

/**
 * Renames an existing property on a record.
 */
class RenamePropertyRecordUpgrader implements RecordUpgrader
{
    private String oldName;
    private String newName;

    /**
     * @param oldName the name of the existing property which will be renamed
     *                - note that this must be a simple propery, not a nested
     *                record
     * @param newName the new name for the property, which must not clash with
     *                existing names
     */
    public RenamePropertyRecordUpgrader(String oldName, String newName)
    {
        this.oldName = oldName;
        this.newName = newName;
    }

    public void upgrade(String path, MutableRecord record)
    {
        Object value = record.remove(oldName);
        if (value == null)
        {
            // We may already have run, just bail now.
            return;
        }

        if (!RecordUpgradeUtils.isSimpleValue(value))
        {
            throw new IllegalArgumentException("Attempt to rename a non-simple property (existing value has type '" + value.getClass() + "')");
        }

        if (record.containsKey(newName))
        {
            throw new IllegalArgumentException("Record already contains a property with name '" + newName + "'");
        }

        record.put(newName, value);
    }
}
