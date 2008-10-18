package com.zutubi.pulse.core.scm.git;

import com.zutubi.util.SystemUtils;

/**
 * Shared constants used by the git implementation.
 */
public class GitConstants
{
    public static final String PROPERTY_GIT_COMMAND = "pulse.git.command";

    /**
     * The git executable
     */
    public static final String GIT;
    static
    {
        if (System.getProperties().contains(PROPERTY_GIT_COMMAND))
        {
            GIT = System.getProperty(PROPERTY_GIT_COMMAND);
        }
        else
        {
            if (SystemUtils.IS_WINDOWS)
            {
                GIT = "git.exe";
            }
            else
            {
                GIT = "git";
            }
        }
    }

    public static final String COMMAND_PULL = "pull";
    public static final String COMMAND_LOG = "log";
    public static final String COMMAND_CLONE = "clone";
    public static final String COMMAND_CHECKOUT = "checkout";
    public static final String COMMAND_BRANCH = "branch";
    public static final String COMMAND_SHOW = "show";

    public static final String FLAG_BRANCH = "-b";
    public static final String FLAG_NAME_STATUS = "--name-status";
    public static final String FLAG_PRETTY = "--pretty";
    public static final String FLAG_CHANGES = "-n";
    public static final String FLAG_NO_CHECKOUT = "--no-checkout";
    public static final String FLAG_REVERSE = "--reverse";
    public static final String FLAG_DELETE = "-D";

    public static final String REVISION_HEAD = "HEAD";

    public static final String ACTION_EDITED =   "M";
    public static final String ACTION_ADDED =    "A";
    public static final String ACTION_DELETED =  "D";
}
