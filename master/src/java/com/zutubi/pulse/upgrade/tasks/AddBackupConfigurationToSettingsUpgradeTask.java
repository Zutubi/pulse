package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.pulse.restore.BackupConfiguration;

/**
 *
 *
 */
public class AddBackupConfigurationToSettingsUpgradeTask extends AbstractRecordUpgradeTask
{
    public String getName()
    {
        return "Backup Configuration";
    }

    public String getDescription()
    {
        return "Add a new configuration record for the newly added backup configuration.";
    }

    public void doUpgrade(MutableRecord record)
    {
        // looking for the settings record.
        // zutubi.globalConfig
        if ("zutubi.globalConfig".equals(record.getSymbolicName()))
        {
            // we do not expect anything to be in the 'backup' field.
            if (record.containsKey("backup"))
            {
                addError("Unexpected data in the backup field.");
                return;
            }

            // ok, we need to add a new record for the backup configuration here.
            MutableRecord newRecord = new MutableRecordImpl();
            newRecord.setSymbolicName("zutubi.backupConfig");
            newRecord.put("enabled", Boolean.TRUE.toString());
            newRecord.put("cronSchedule", BackupConfiguration.DEFAULT_CRON_SCHEDULE);

            // do we need to add a handle to this record?..

            record.put("backup", newRecord);
        }
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
