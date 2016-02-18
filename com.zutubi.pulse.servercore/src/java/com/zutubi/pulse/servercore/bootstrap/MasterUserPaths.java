package com.zutubi.pulse.servercore.bootstrap;

import java.io.File;

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
     * The project root directory. All working files are stored under this
     * root.
     *
     * @return the directory in which project build artifacts are stored.
     */
    File getProjectRoot();

    /**
     * The base directory storing system backups.
     * 
     * @return the directory into which backup files will be written.
     */
    File getBackupRoot();

    File getRestoreRoot();

    File getUserRoot();

    File getUserTemplateRoot();

    File getRecordRoot();

    File getDriverRoot();

    /**
     * The root directory for the embedded Pulse artifact repository.
     *
     * @return the base directory of the artifact repository.
     */
    File getRepositoryRoot();
}
