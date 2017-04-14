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
import com.zutubi.tove.type.record.MutableRecordImpl;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * Adds the new conditions collection to triggers.
 */
public class TriggerConditionsUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_TRIGGERS = "triggers";
    private static final String PROPERTY_CONDITIONS = "conditions";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Map<String,Record> triggerRecords = recordManager.selectAll(getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_TRIGGERS, WILDCARD_ANY_ELEMENT));
        for (Map.Entry<String, Record> pathRecord: triggerRecords.entrySet())
        {
            recordManager.insert(getPath(pathRecord.getKey(), PROPERTY_CONDITIONS), new MutableRecordImpl());
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}