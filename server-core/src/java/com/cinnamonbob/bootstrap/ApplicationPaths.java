package com.cinnamonbob.bootstrap;

import java.io.File;
import java.util.List;

/**
 * Defines the location of application paths, including paths for input
 * configuration files and paths for storing system data.
 */
public interface ApplicationPaths
{
    /**
     * The system root directory, under which all system files should be
     * stored.  This is split into some more specific directories below.
     *
     * @return the directory to use as the system root
     */
    File getSystemRoot();

    /**
     * The www root directory.  Used as the base for deploying a web
     * application.
     *
     * @return the directory to use as the root for the web app
     */
    File getContentRoot();

    /**
     * Returns the system configuration directory.  Configuration files in
     * this directory are not normally edited by the user.  An example is
     * logging configuration, which may be edited in special circumstances
     * (e.g. debugging)
     *
     * @return the directory to use for system configuration files
     */
    File getConfigRoot();

    /**
     * The user configuration directory.  Configuration files in this
     * directory are expected to be edited by the user.  A prime example is
     * basic system properties such as the port for the web application.
     *
     * @return the directory to use for user configuration files
     */
    File getUserConfigRoot();

    /**
     * The template root directories.  All velocity templates are nested under
     * these directories.
     *
     * @return a list of the directories that contain all velocity templates
     */
    List<File> getTemplateRoots();

    /**
     * The database root directory.  All database files are stored under this
     * root.
     *
     * @return the directory to use as the database root
     */
    File getDatabaseRoot();
}
