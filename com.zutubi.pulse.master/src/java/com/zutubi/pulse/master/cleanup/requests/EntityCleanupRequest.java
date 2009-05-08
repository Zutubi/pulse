package com.zutubi.pulse.master.cleanup.requests;

import com.zutubi.pulse.master.cleanup.CleanupRequest;
import com.zutubi.pulse.core.model.Entity;

/**
 * A base cleanup request for entity instances that implements the equals and
 * hashCode methods such that requests for the same entity are considered the
 * same. 
 */
public abstract class EntityCleanupRequest implements CleanupRequest
{
    private Entity entity;

    protected EntityCleanupRequest(Entity entity)
    {
        this.entity = entity;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        EntityCleanupRequest that = (EntityCleanupRequest) o;
        return entity.equals(that.entity);
    }

    public int hashCode()
    {
        return entity.hashCode();
    }
}
