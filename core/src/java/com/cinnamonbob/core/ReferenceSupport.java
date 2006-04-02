package com.cinnamonbob.core;

/**
 * A base class to simplify references implemented in the common way.
 */
public class ReferenceSupport implements Reference
{
    private String name;

    public ReferenceSupport()
    {

    }
    
    public ReferenceSupport(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return this;
    }
}
