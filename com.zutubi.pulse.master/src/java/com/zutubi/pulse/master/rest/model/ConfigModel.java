package com.zutubi.pulse.master.rest.model;

/**
 * Toy model class while working on RESTish API.
 */
public abstract class ConfigModel
{
    private TypeModel type;

    protected ConfigModel(TypeModel type)
    {
        this.type = type;
    }

    public TypeModel getType()
    {
        return type;
    }
}
