package com.zutubi.tove.config;

import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.Record;

/**
 * Base for record walking functions that process references in those
 * records in some way.
 */
abstract class ReferenceWalkingFunction extends TypeAwareRecordWalkingFunction
{
    public ReferenceWalkingFunction(ComplexType type, Record record, String path)
    {
        super(type, record, path);
    }

    protected  void process(String path, Record record, Type type)
    {
        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            for (TypeProperty property: compositeType.getProperties(ReferenceType.class))
            {
                String value = (String) record.get(property.getName());
                if (value != null)
                {
                    handleReference(path, record, property, value);
                }
            }

            for (TypeProperty property: compositeType.getProperties(ListType.class))
            {
                Type targetType = property.getType().getTargetType();
                if (targetType instanceof ReferenceType)
                {
                    String[] value = (String[]) record.get(property.getName());
                    if (value != null)
                    {
                        handleReferenceList(path, record, property, value);
                    }
                }
            }
        }
    }

    protected abstract void handleReferenceList(String path, Record record, TypeProperty property, String[] value);
    protected abstract void handleReference(String path, Record record, TypeProperty property, String value);
}
