package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.type.ComplexType;

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
}
