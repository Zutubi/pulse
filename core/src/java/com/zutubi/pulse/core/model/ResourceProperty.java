package com.zutubi.pulse.core.model;

/**
 */
public class ResourceProperty
{
    private String name;
    private String value;
    private boolean addToEnvironment = false;
    private boolean addToPath = false;

    public ResourceProperty()
    {
    }

    public ResourceProperty(String name, String value, boolean addToEnvironment, boolean addToPath)
    {
        this.name = name;
        this.value = value;
        this.addToEnvironment = addToEnvironment;
        this.addToPath = addToPath;
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
}
