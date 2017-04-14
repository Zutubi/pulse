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

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * Changes a user's default action to 'browse' when it is currently
 * 'projects' (CIB-1582).
 */
public class UpdateDefaultActionUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Map<String,Record> preferencesRecords = recordManager.selectAll(PathUtils.getPath("users/*/preferences"));
        for(Map.Entry<String, Record> preferencesEntry: preferencesRecords.entrySet())
        {
            Record record = preferencesEntry.getValue();
            String currentDefault = (String) record.get("defaultAction");
            if("projects".equals(currentDefault))
            {
                MutableRecord mutable = record.copy(false, true);
                mutable.put("defaultAction", "browse");
                recordManager.update(preferencesEntry.getKey(), mutable);
            }
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
