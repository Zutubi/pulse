package com.cinnamonbob.core.model;

/**
 * 
 *
 */
public class Entity
{
    private long id;

    public long getId()
    {
        return id;
    }

    private void setId(long id)
    {
        this.id = id;
    }

    public boolean isPersistent()
    {
        return this.id != 0;
    }

    public boolean equals(Object other)
    {
        if (!(other instanceof Entity))
        {
            return false;
        }
        Entity otherEntity = (Entity) other;
        if (id == 0 || otherEntity.id == 0)
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
