package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;

/**
 * A base class to simplify references that are named objects where the
 * reference value is the object itself.
 */
public class SelfReference implements Reference
{
    private String name;

    public SelfReference()
    {

    }
    
    public SelfReference(String name)
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
