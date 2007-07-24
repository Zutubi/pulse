package com.zutubi.prototype.model;

import com.zutubi.prototype.type.CompositeType;

/**
 * Holds data about a single step in a wizard.
 */
public class WizardStep
{
    private String id;
    private CompositeType type;
    private String name;

    public WizardStep(String id, CompositeType type, String name)
    {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public CompositeType getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }
}
