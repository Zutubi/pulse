package com.zutubi.pulse.core.model;

import com.zutubi.util.Predicate;

/**
 * Predicate to test the id of entities against a fixed id.
 */
public class EntityWithIdPredicate<T extends Entity> implements Predicate<T>
{
    private final long id;

    public EntityWithIdPredicate(long id)
    {
        this.id = id;
    }

    public boolean satisfied(T entity)
    {
        return entity.getId() == id;
    }
}
