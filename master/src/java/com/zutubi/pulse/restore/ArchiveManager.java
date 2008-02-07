package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public interface ArchiveManager
{
    ProgressMonitor getMonitor();

    Archive prepareRestore(File backup) throws ArchiveException;

    Archive previewRestore();

    void restoreArchive();

    Archive createArchive();

    void restoreArchive(Archive archive);

    // restore on restart handling.

    void cancelRestoreOnRestart();

    void requestRestoreOnRestart(Archive archive);

    boolean isRestoreOnRestartRequested();

    Archive getArchiveToBeRestoredOnRestart();

    // auto archive support.
}
