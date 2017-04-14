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
 * A record upgrader that changes the symbolic name of the record.
 */
class ChangeSymbolicNameRecordUpgrader implements RecordUpgrader
{
    private String oldSymbolicName;
    private String newSymbolicName;

    /**
     * Creates a new upgrader that will change all occurrences of the given old
     * name to the given new name.
     *
     * @param oldSymbolicName the symbolic name to rename
     * @param newSymbolicName the new symbolic name
     */
    public ChangeSymbolicNameRecordUpgrader(String oldSymbolicName, String newSymbolicName)
    {
        this.oldSymbolicName = oldSymbolicName;
        this.newSymbolicName = newSymbolicName;
    }

    public void upgrade(String path, MutableRecord record)
    {
        if (record.getSymbolicName().equals(oldSymbolicName))
        {
            record.setSymbolicName(newSymbolicName);
        }
    }
}
