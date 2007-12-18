package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public class DefaultRestoreManager implements RestoreManager
{
    private RestoreProgressMonitor restoreMonitor = new RestoreProgressMonitor();

    private BackupInfo backupInfo;

    public RestoreProgressMonitor getRestoreMonitor()
    {
        return restoreMonitor;
    }

    public BackupInfo prepareRestore(File backup)
    {
        // check the backup file, load the backup info.
        backupInfo = new BackupInfo();
        backupInfo.setSource(backup);
        
        return backupInfo;
    }

    public BackupInfo previewRestore()
    {
        return backupInfo;
    }

    public void executeRestore()
    {
        restoreMonitor.start();

        // process the restore
        // a) restore the configuration.

        // - restore the pulse.config.properties file.
        // - restore the records
        // - restore the plugins
        // - restore the config/* w/o   database.properties (optional).
        //      if the restore contains a data export, then import into the database
        //      if the restore does not contain a data export, expect it to contain a database.properties file.
        //      if the restore does not contain a data export and no database.properties, use the default.

        // required resources:
        //      - record store
        //      - database
        //      - configuration
        //  ie: the external integration points.

        restoreMonitor.finish();
    }
}
