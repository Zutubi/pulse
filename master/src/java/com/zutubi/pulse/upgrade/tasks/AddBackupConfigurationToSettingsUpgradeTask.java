package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.monitor.TaskException;
import com.zutubi.pulse.restore.BackupConfiguration;

/**
 *
 *
 */
public class AddBackupConfigurationToSettingsUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public String getName()
    {
        return "Backup Configuration";
    }

    public String getDescription()
    {
        return "Add a new configuration record for the newly added backup configuration.";
    }

    public void execute() throws TaskException
    {
        Record globalSettings = recordManager.select("settings");
        if (!"zutubi.globalConfig".equals(globalSettings.getSymbolicName()))
        {
            // this is unexpected - we must have the path wrong.
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
