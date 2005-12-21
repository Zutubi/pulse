package com.cinnamonbob.core;

/**
 * 
 *
 */
public class SimpleReference implements Reference
{
    private String name;
    private Reference ref;

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return this;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Reference getRef()
    {
        return ref;
    }

    public void setRef(Reference ref)
    {
        this.ref = ref;
    }
}
