package com.zutubi.pulse.core.scm.git;

import com.zutubi.util.SystemUtils;

/**
 * Shared constants used by the git implementation.
 */
public class GitConstants
{
    /**
     * Pulse system property used to override the default git command line executable.
     */
    public static final String PROPERTY_GIT_COMMAND = "pulse.git.command";

    /**
     * The default git executable, usually "git" but "git.exe" on a windows
     * system.  May be overridden with a system property.
     */
    public static final String DEFAULT_GIT = SystemUtils.IS_WINDOWS ? "git.exe" : "git";

    public static final String COMMAND_PULL = "pull";
    public static final String COMMAND_LOG = "log";
    public static final String COMMAND_CLONE = "clone";
    public static final String COMMAND_CHECKOUT = "checkout";
    public static final String COMMAND_BRANCH = "branch";
    public static final String COMMAND_SHOW = "show";
    public static final String COMMAND_DIFF = "diff";
    public static final String COMMAND_LS_REMOTE = "ls-remote";

    public static final String FLAG_BRANCH = "-b";
    public static final String FLAG_NAME_STATUS = "--name-status";
    public static final String FLAG_PRETTY = "--pretty";
    public static final String FLAG_CHANGES = "-n";
    public static final String FLAG_NO_CHECKOUT = "--no-checkout";
    public static final String FLAG_REVERSE = "--reverse";
    public static final String FLAG_DELETE = "-D";
    public static final String FLAG_SHOW_MERGE_FILES = "-c";

    public static final String REVISION_HEAD = "HEAD";

    /**
     * File has conflicts after a merge
     */
    public static final String ACTION_UNMERGED =   "U";
    /**
     * File has been copied and modified
     */
    public static final String ACTION_COPY_MODIFIED =   "C";
    /**
     * File has been renamed and modified
     */
    public static final String ACTION_RENAME_MODIFIED =   "R";
    /**
     * File has been modified
     */
    public static final String ACTION_MODIFIED =   "M";
    /**
     * File has been added
     */
    public static final String ACTION_ADDED =    "A";
    /**
     * File has been deleted
     */
    public static final String ACTION_DELETED =  "D";
}
