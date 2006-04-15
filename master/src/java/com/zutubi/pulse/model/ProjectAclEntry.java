package com.zutubi.pulse.model;

import org.acegisecurity.acl.basic.SimpleAclEntry;

/**
 */
public class ProjectAclEntry extends SimpleAclEntry
{
    private static final int UNSAVED = 0;
    private long id;

    protected ProjectAclEntry()
    {

    }
    
    public ProjectAclEntry(String recipient, Project identity, int mask)
    {
        super(recipient, identity, null, mask);
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public boolean isPersistent()
    {
        return this.id != UNSAVED;
    }

    public boolean equals(Object other)
    {
        if (!(other instanceof ProjectAclEntry))
        {
            return false;
        }
        ProjectAclEntry otherEntity = (ProjectAclEntry) other;
        if (id == UNSAVED || otherEntity.id == UNSAVED)
        {
            return false;
        }
        return id == otherEntity.id;
    }

    public int hashCode()
    {
        return Long.valueOf(id).hashCode();
    }

}
