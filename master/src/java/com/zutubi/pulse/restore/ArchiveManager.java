package com.zutubi.pulse.restore;

import com.zutubi.pulse.restore.feedback.TaskMonitor;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public interface ArchiveManager
{
    TaskMonitor getTaskMonitor();

    Archive prepareRestore(File backup) throws ArchiveException;

    List<RestoreTask> previewRestore();

    void restoreArchive();

    Archive createArchive() throws ArchiveException;

    void restoreArchive(Archive archive);

    // restore on restart handling.

    void cancelRestoreOnRestart();

    void requestRestoreOnRestart(Archive archive);

    boolean isRestoreOnRestartRequested();

    Archive getArchiveToBeRestoredOnRestart();

    // auto archive support.


    //-----------------------------------------------------------


    Archive getArchive();
}
