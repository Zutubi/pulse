package com.cinnamonbob.core.model;

/**
 * 
 *
 */
public class Entity
{
    private long id;

    private static final int UNSAVED = 0;

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
        if (!(other instanceof Entity))
        {
            return false;
        }
        Entity otherEntity = (Entity) other;
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
