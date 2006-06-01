package com.zutubi.pulse.bootstrap;

import java.io.File;
import java.util.List;

/**
 * <class-comment/>
 */
public interface SystemPaths
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
     * The template root directories.  All velocity templates are nested under
     * these directories.
     *
     * @return a list of the directories that contain all velocity templates
     */
    List<File> getTemplateRoots();

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
     * Returns the directory to use for log files.
     *
     * @return the logs directory
     */
    File getLogRoot();

    /**
     * Returns the root directory of the systems temporary space.
     *
     * @return the directory used for temporary files.
     */
    File getTmpRoot();
}
