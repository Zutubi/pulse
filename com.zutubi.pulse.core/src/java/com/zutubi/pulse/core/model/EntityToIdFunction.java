package com.zutubi.pulse.core.model;

import com.google.common.base.Function;

/**
 * Maps from entities to their ids.
 */
public class EntityToIdFunction<T extends Entity> implements Function<T, Long>
{
    public Long apply(T t)
    {
        return t.getId();
    }
}
