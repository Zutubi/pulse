package com.zutubi.pulse.restore;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public interface ArchiveableComponent
{
    /**
     * Unique identifier for this restorable component.
     *
     * @return a name for this archiveable instance
     */
    String getName();

    /**
     * Backup the persistent state to the specified archive
     *
     * @param archive to write the backup information to.
     *
     * @throws ArchiveException if the archive process encounters any problems.
     */
    void backup(File archive) throws ArchiveException;

    /**
     * Restore the persistent state from the specified archive.
     *
     * @param archive to read the backup information from.
     *
     * @throws ArchiveException if the archive process encounters any problems.
     */
    void restore(File archive) throws ArchiveException;

    List<RestoreTask> getRestoreTasks(File archiveComponentBase);
}
