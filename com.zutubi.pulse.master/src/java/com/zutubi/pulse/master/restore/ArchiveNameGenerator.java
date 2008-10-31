package com.zutubi.pulse.master.restore;

import java.io.File;

/**
 * The base interface to be implemented by all archive name generation
 * algorithms.
 */
public interface ArchiveNameGenerator
{
    /**
     * Generate a new archive name for an archive that will be
     * placed in the target directory.
     *
     * @param target directory into which the newly named archive will
     * be placed.
     *
     * @return the name of the new file.
     */
    String newName(File target);

    boolean matches(String name);
}
