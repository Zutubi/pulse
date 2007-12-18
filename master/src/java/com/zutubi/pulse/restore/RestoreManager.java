package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public interface RestoreManager
{
    RestoreProgressMonitor getRestoreMonitor();

    BackupInfo prepareRestore(File backup);

    BackupInfo previewRestore();

    void executeRestore();
}
