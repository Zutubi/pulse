package com.zutubi.pulse.core.scm.git;

/**
 * The GitBranchEntry is a value object that holds the details of a response
 * from the git branch command. 
 */
class GitBranchEntry
{
    /**
     * Indicates whether or not the workspace is currently set to this branch.
     */
    private boolean active;
    /**
     * The name of the branch associated with this entry.
     */
    private String name;

    GitBranchEntry(boolean active, String name)
    {
        this.active = active;
        this.name = name;
    }

    boolean isActive()
    {
        return active;
    }

    String getName()
    {
        return name;
    }
}
