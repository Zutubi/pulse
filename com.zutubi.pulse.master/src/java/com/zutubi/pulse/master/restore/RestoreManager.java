package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.Task;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public interface RestoreManager
{
    Monitor getMonitor();

    Archive prepareRestore(File backup) throws ArchiveException;

    List<Task> previewRestore();

    void restoreArchive();

    File postRestore();

    Archive createArchive() throws ArchiveException;

    //--( proposed interface to make this much more of a manager, not yet implemented )---
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
