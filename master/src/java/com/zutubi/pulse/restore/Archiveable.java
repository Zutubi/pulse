package com.zutubi.pulse.restore;

/**
 *
 *
 */
public interface Archiveable
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
    void backup(Archive archive) throws ArchiveException;

    /**
     * Restore the persistent state from the specified archive.
     *
     * @param archive to read the backup information from.
     *
     * @throws ArchiveException if the archive process encounters any problems.
     */
    void restore(Archive archive) throws ArchiveException;
}
