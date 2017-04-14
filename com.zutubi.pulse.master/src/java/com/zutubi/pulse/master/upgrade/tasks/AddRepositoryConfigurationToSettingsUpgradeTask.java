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
import com.zutubi.pulse.master.restore.BackupConfiguration;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Add the record for the RepositoryConfiguration to the global settings.
 */
public class AddRepositoryConfigurationToSettingsUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public void execute() throws TaskException
    {
        Record globalSettings = recordManager.select("settings");
        if (globalSettings.containsKey("repository"))
        {
            // we already have a repository record? ok, in that case, we do not need to insert one.
            return;
        }

        MutableRecord newRecord = new MutableRecordImpl();
        newRecord.setSymbolicName("zutubi.repositoryConfig");
        newRecord.put("readAccess", new String[0]);
        newRecord.put("writeAccess", new String[0]);

        recordManager.insert("settings/repository", newRecord);
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}