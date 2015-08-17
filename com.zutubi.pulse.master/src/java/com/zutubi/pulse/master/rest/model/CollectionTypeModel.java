package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.type.CollectionType;

/**
 * Model wrapping collection type.
 */
public class CollectionTypeModel extends TypeModel
{
    private CollectionType collectionType;

    public CollectionTypeModel(CollectionType type)
    {
        super(type);
        collectionType = type;
    }

    public boolean isOrdered()
    {
        return collectionType.isOrdered();
    }

    public String getTargetShortType()
    {
        return formatShortType(collectionType.getTargetType());
    }
}
