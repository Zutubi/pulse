package com.zutubi.pulse.restore;

import com.zutubi.pulse.monitor.Monitor;
import com.zutubi.pulse.monitor.Task;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public interface ArchiveManager
{
    Monitor getTaskMonitor();

    Archive prepareRestore(File backup) throws ArchiveException;

    List<Task> previewRestore();

    void restoreArchive();

    File postRestore();

    Archive createArchive() throws ArchiveException;

    //--( proposed interface, not yet implemented )---
/*
    void restoreArchive(Archive archive);

    void cancelRestoreOnRestart();

    void requestRestoreOnRestart(Archive archive);

    boolean isRestoreOnRestartRequested();

    Archive getArchiveToBeRestoredOnRestart();
*/

    // auto archive support.


    //-----------------------------------------------------------


    Archive getArchive();
}
