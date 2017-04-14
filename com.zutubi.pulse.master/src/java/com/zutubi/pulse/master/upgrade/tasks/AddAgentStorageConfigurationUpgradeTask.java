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

import com.google.common.base.Function;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.StringUtils;

/**
 * Adds the new agent storage configuration.
 */
public class AddAgentStorageConfigurationUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE = "agents";
    private static final String PROPERTY_DATA_DIRECTORY = "dataDirectory";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE, recordManager);
        details.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                String path = PathUtils.getPath("agents", node.getId(), "storage");
                if (recordManager.containsRecord(path))
                {
                    return true;
                }
                
                MutableRecord storageRecord = new MutableRecordImpl();
                storageRecord.setSymbolicName("zutubi.agentStorageConfig");
                if (node.getParent() == null)
                {
                    String ownerId = node.getId();
                    Record agentRecord = recordManager.select(SCOPE + "/" + ownerId);
                    String dataDirectory = agentRecord == null ? null : (String) agentRecord.get(PROPERTY_DATA_DIRECTORY);
                    if (!StringUtils.stringSet(dataDirectory))
                    {
                        dataDirectory = "$(data.dir)/agents/$(agent.handle)";
                    }

                    storageRecord.put(PROPERTY_DATA_DIRECTORY, dataDirectory);
                    storageRecord.put("outsideCleanupAllowed", "false");
                    storageRecord.put("diskSpaceThresholdEnabled", "false");
                    storageRecord.put("diskSpaceThresholdMib", "128");
                }

                recordManager.insert(path, storageRecord);
                return true;
            }
        });
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
