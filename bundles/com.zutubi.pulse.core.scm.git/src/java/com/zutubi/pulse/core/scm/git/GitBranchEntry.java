package com.zutubi.pulse.core.scm.git;

/**
 *
 *
 */
public class GitBranchEntry
{
    private boolean active;
    private String name;

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
