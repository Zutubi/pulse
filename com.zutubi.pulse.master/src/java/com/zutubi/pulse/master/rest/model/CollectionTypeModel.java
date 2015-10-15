package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;

/**
 * Model wrapping collection type.
 */
public class CollectionTypeModel extends TypeModel
{
    private CollectionType collectionType;
    private CompositeTypeModel targetType;

    public CollectionTypeModel(CollectionType type)
    {
        super();
        collectionType = type;

        Type targetType = type.getTargetType();
        if (targetType instanceof CompositeType)
        {
            this.targetType = new CompositeTypeModel((CompositeType) targetType);
        }
    }

    public boolean isOrdered()
    {
        return collectionType.isOrdered();
    }

    public String getTargetShortType()
    {
        return formatShortType(collectionType.getTargetType());
    }

    public CompositeTypeModel getTargetType()
    {
        return targetType;
    }
}
