package com.zutubi.prototype.model;

/**
 * Holds data about a single step in a wizard.
 */
public class WizardStep
{
    private String id;
    private String name;

    public WizardStep(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }
}
