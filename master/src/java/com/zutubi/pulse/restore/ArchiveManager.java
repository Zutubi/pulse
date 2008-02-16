package com.zutubi.pulse.restore;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public interface ArchiveManager
{
    ProgressMonitor getMonitor();

    Archive prepareRestore(File backup) throws ArchiveException;

    List<RestoreTaskGroup> previewRestore();

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


}
