package com.zutubi.pulse.core.model;

import com.zutubi.pulse.model.NamedEntity;

/**
 */
public class ResourceProperty extends Entity implements NamedEntity
{
    private String name;
    private String value;
    private boolean addToEnvironment = false;
    private boolean addToPath = false;
    private boolean resolveVariables = false;

    public ResourceProperty()
    {
    }

    public ResourceProperty(String name, String value, boolean addToEnvironment, boolean addToPath, boolean resolveVariables)
    {
        this.name = name;
        this.value = value;
        this.addToEnvironment = addToEnvironment;
        this.addToPath = addToPath;
        this.resolveVariables = resolveVariables;
    }

    public ResourceProperty copy()
    {
        return new ResourceProperty(name, value, addToEnvironment, addToPath, resolveVariables);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public boolean getAddToEnvironment()
    {
        return addToEnvironment;
    }

    public void setAddToEnvironment(boolean addToEnvironment)
    {
        this.addToEnvironment = addToEnvironment;
    }

    public boolean getAddToPath()
    {
        return addToPath;
    }

    public void setAddToPath(boolean addToPath)
    {
        this.addToPath = addToPath;
    }

    public boolean getResolveVariables()
    {
        return resolveVariables;
    }

    public void setResolveVariables(boolean resolveVariables)
    {
        this.resolveVariables = resolveVariables;
    }
}
