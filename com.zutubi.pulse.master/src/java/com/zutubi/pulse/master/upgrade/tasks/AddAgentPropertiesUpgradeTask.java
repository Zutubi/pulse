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

import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;

/**
 * Upgrade task to add the properties collection to instances of AgentConfiguration.
 */
public class AddAgentPropertiesUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE = "agents";
    private static final String PROPERTY = "properties";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    public void execute()
    {
        Map<String,Record> agentRecords = recordManager.selectAll(getPath(SCOPE, WILDCARD_ANY_ELEMENT));
        for (Map.Entry<String, Record> pathRecord: agentRecords.entrySet())
        {
            recordManager.insert(getPath(pathRecord.getKey(), PROPERTY), new MutableRecordImpl());
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
