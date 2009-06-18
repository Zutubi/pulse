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