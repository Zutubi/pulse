package com.zutubi.pulse.master.xwork.actions.project;

/**
 * Holds information about project responsibility for JSON transfer.
 */
public class ProjectResponsibilityModel
{
    private String owner; // foo is responsible for...
    private String comment; // optional
    private boolean canClear;

    public ProjectResponsibilityModel(String owner, String comment)
    {
        this.owner = owner;
        this.comment = comment;
    }

    public String getOwner()
    {
        return owner;
    }

    public String getComment()
    {
        return comment;
    }

    public boolean isCanClear()
    {
        return canClear;
    }

    public void setCanClear(boolean canClear)
    {
        this.canClear = canClear;
    }
}
