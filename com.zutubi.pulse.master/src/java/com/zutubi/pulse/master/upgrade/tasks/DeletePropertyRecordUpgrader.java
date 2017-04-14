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
 * Deletes an existing simple property from a record.
 */
class DeletePropertyRecordUpgrader implements RecordUpgrader
{
    private String name;

    /**
     * @param name the name of the property to delete, must be a simple
     *        property (not a nested record)
     */
    public DeletePropertyRecordUpgrader(String name)
    {
        this.name = name;
    }

    public void upgrade(String path, MutableRecord record)
    {
        Object value = record.remove(name);
        if (value != null && !RecordUpgradeUtils.isSimpleValue(value))
        {
            throw new IllegalArgumentException("Attempt to delete a non-simple value (existing value of property '" + name + "' has type '" + value.getClass() + "')");
        }
    }
}
