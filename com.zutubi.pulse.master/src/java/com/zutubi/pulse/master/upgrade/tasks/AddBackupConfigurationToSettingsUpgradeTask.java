package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.master.restore.BackupConfiguration;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 *
 *
 */
public class AddBackupConfigurationToSettingsUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public void execute() throws TaskException
    {
        Record globalSettings = recordManager.select("settings");
        if (!"zutubi.globalConfig".equals(globalSettings.getSymbolicName()))
        {
            // this is unexpected - we must have the path wrong.
        }

        if (globalSettings.containsKey("backup"))
        {
            // we already have a backup record? ok, in that case, we do not need to insert one.
            return;
        }

        // ok, we need to add a new record for the backup configuration here.
        MutableRecord newRecord = new MutableRecordImpl();
        newRecord.setSymbolicName("zutubi.backupConfig");
        newRecord.put("enabled", Boolean.TRUE.toString());
        newRecord.put("cronSchedule", BackupConfiguration.DEFAULT_CRON_SCHEDULE);

        recordManager.insert("settings/backup", newRecord);
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
