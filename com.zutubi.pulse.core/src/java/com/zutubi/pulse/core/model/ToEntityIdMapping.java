package com.zutubi.pulse.core.model;

import com.zutubi.util.Mapping;

/**
 * Maps from entities to their ids.
 */
public class ToEntityIdMapping<T extends Entity> implements Mapping<T, Long>
{
    public Long map(T t)
    {
        return t.getId();
    }
}
