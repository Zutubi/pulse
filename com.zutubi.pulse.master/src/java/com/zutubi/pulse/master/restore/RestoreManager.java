package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.Task;

import java.io.File;
import java.util.List;

/**
 * The restore manager handles the restoration process on
 * startup.
 */
public interface RestoreManager
{
    /**
     * Return a monitor instance for the current restoration process.  If
     * no restoration has been prepared, then null is returned.
     *
     * @return a monitor
     *
     * @see #prepareRestore(java.io.File) 
     */
    Monitor getMonitor();

    /**
     * Schedule the restoration from the specified backup file.  This method
     * will return an archive instance that represents the backup, providing
     * access to the details of the backup being restored.
     *
     * @param backup    the file that contains a valid backup of the pulse.
     * @return an archive.
     * @throws ArchiveException on error
     */
    Archive prepareRestore(File backup) throws ArchiveException;

    /**
     * Get the list of tasks that will be executed by this restoration process.
     * This preview will return null if no restoration has been scheduled.
     *
     * @return a list of tasks.
     *
     * @see #getMonitor() 
     */
    List<Task> previewRestore();

    /**
     * Start the archive restoration process.  Once restored, the original archive
     * file is available via the  {@link RestoreManager#getBackedupArchive()} method.
     *
     * @throws ArchiveException on error or if no archive has been prepared.
     */
    void restoreArchive() throws ArchiveException;

    /**
     * Get the backup of the archive used in the restoration process.
     * @return an archive file.
     */
    File getBackedupArchive();

    /**
     * The archive that was provided by the latest {@link RestoreManager#prepareRestore(java.io.File)} call.
     * @return an archive, or null if prepareRestore has not been called.
     */
    Archive getArchive();

}
