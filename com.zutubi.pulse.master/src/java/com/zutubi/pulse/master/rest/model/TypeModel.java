package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.type.*;

/**
 * Model for type information.
 */
public class TypeModel
{
    private ComplexType type;

    public TypeModel(ComplexType type)
    {
        this.type = type;
    }

    public String getSymbolicName()
    {
        return type.getSymbolicName();
    }

    public static String formatShortType(Type type)
    {
        if (type instanceof CollectionType)
        {
            String collectionType;
            if (type instanceof ListType)
            {
                collectionType = "List";
            }
            else
            {
                collectionType = "Map";
            }

            return collectionType + "<" + formatShortType(type.getTargetType()) + ">";
        }
        else if (type instanceof ReferenceType)
        {
            return "Reference<" + formatShortType(((ReferenceType) type).getReferencedType()) +">";
        }
        else if (type instanceof EnumType)
        {
            return "Enum";
        }
        else
        {
            return type.getSymbolicName();
        }
    }
}
