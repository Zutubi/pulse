package com.zutubi.tove.ui.model;

import com.zutubi.i18n.Messages;
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
    private String targetLabel;

    public CollectionTypeModel(CollectionType type)
    {
        super();
        collectionType = type;

        Type targetType = type.getTargetType();
        if (targetType instanceof CompositeType)
        {
            this.targetType = new CompositeTypeModel((CompositeType) targetType);
            targetLabel = Messages.getInstance(targetType.getClazz()).format("label");
        }
    }

    public boolean isOrdered()
    {
        return collectionType.isOrdered();
    }

    public boolean isKeyed()
    {
        return collectionType.hasSignificantKeys();
    }

    public String getTargetShortType()
    {
        return formatShortType(collectionType.getTargetType());
    }

    public CompositeTypeModel getTargetType()
    {
        return targetType;
    }

    public String getTargetLabel()
    {
        return targetLabel;
    }
}
