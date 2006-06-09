package com.zutubi.pulse.bootstrap;

import java.io.File;

/**
 * <class-comment/>
 */
public interface MasterUserPaths extends UserPaths
{
    /**
     * The database root directory.  All database files are stored under this
     * root.
     *
     * @return the directory to use as the database root
     */
    File getDatabaseRoot();

    /**
     * The project root directory. All working files are
     *
     * @return the directory in which project build artifacts are stored.
     */
    File getProjectRoot();
}
