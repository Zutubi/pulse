package com.zutubi.pulse.master.xwork.actions.admin.debug;

import com.zutubi.pulse.master.restore.BackupManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Simple action to manually trigger the systems backup.
 */
public class TriggerBackupAction extends ActionSupport
{
    private BackupManager backupManager;

    public String execute() throws Exception
    {
        backupManager.triggerBackup();
        
        return super.execute();
    }

    public void setBackupManager(BackupManager backupManager)
    {
        this.backupManager = backupManager;
    }
}
