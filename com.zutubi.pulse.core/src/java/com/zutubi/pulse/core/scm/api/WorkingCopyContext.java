package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.util.config.Config;

import java.io.File;

/**
 * The context in which working copy operations are run.
 */
public interface WorkingCopyContext
{
    /**
     * Indicates the base directory for a working copy operation.
     *
     * @return the working directory for the working copy operation, usually
     *         the root of a working copy, although implementations may locate
     *         the root in other ways if desired (e.g. to match the behaviour
     *         of the SCMs other tools)
     */
    File getBase();

    /**
     * Returns the configuration for the working copy operation.  These are
     * properties which can be configured by the user in a configuration file
     * and/or on the command line.
     * <p/>
     * The configuration contains properties used by Pulse itself, but each
     * implementation may define and use their own properties as required.  To
     * avoid conflicts, each implementation should begin all of its own
     * property names with a prefix based on the SCM name.  For consistency,
     * periods should be used to separate words in the property key.  For
     * example, to configure the password for a Subversion repository the
     * Subversion implementation defines a property named "svn.password".
     *
     * @return user configuration to customise the working copy operation
     */
    Config getConfig();

    /**
     * Returns a user interface that can be used to interact with the user
     * during working copy operations.
     * 
     * @return interface with the user running the operation
     */
    UserInterface getUI();
}
