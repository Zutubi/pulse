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
import com.zutubi.tove.type.record.*;

import java.util.Map;

/**
 * Adds records for the new BrowseViewSettingsConfiguration to users.
 */
public class AddBrowseViewConfigurationUpgradeTask extends AbstractUpgradeTask
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
            if (record.containsKey("browseView"))
            {
                continue;
            }

            MutableRecord newRecord = new MutableRecordImpl();
            newRecord.setSymbolicName("zutubi.browseViewConfig");
            newRecord.setPermanent(true);
            newRecord.put("groupsShown", Boolean.TRUE.toString());
            newRecord.put("hierarchyShown", Boolean.TRUE.toString());
            newRecord.put("hiddenHierarchyLevels", Integer.toString(1));
            newRecord.put("buildsPerProject", Integer.toString(1));
            newRecord.put("columns", new String[]{"when", "elapsed", "reason", "tests"});

            recordManager.insert(preferencesEntry.getKey() + "/browseView", newRecord);
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
