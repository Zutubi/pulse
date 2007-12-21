package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public interface Restorable
{
    /**
     * Unique identifier for this restorable component.
     *
     * @return
     */
    String getName();

    /**
     * Backup the persistent state to the specified directory.
     *
     * @param base
     */
    void backup(File base) throws RestoreException;

    /**
     * Restore the persistent state from the specified directory.
     *
     * @param base
     */
    void restore(File base) throws RestoreException;
}
