package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.restore.BackupManager;
import com.zutubi.pulse.master.upgrade.UpgradeException;
import com.zutubi.util.logging.Logger;

/**
 * Trigger a backup before continuing with the upgrade.
 */
public class BackupUpgradeTask extends AbstractUpgradeTask 
{
    private static final Logger LOG = Logger.getLogger(BackupUpgradeTask.class);
    
    private BackupManager backupManager;

    public int getBuildNumber()
    {
        // -/ve indicates that this build number should not be recorded against the target data directory. 
        return -1;
    }

    public void execute() throws UpgradeException
    {
        try
        {
            backupManager.triggerBackup();
        }
        catch (Exception e)
        {
            LOG.severe(e);
            addError(e.getMessage());
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setBackupManager(BackupManager backupManager)
    {
        this.backupManager = backupManager;
    }
}
