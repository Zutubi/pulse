package com.zutubi.pulse.servercore.bootstrap;

import java.io.File;

/**
 * User paths common to both the master and build agents.
 */
public interface UserPaths
{
    /**
     * The pulse data directory is the place where all of the users data is
     * stored.
     *
     * @return the data directory for all pulse data
     */
    File getData();

    /**
     * The user configuration directory.  Configuration files in this
     * directory are expected to be edited by the user.  A prime example is
     * basic system properties such as the port for the web application.
     *
     * @return the directory to use for user configuration files
     */
    File getUserConfigRoot();
}
