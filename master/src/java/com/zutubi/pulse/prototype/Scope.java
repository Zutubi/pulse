package com.zutubi.pulse.prototype;

/**
 * <class comment/>
 */
public class Scope
{
    private final Scope parent;
    
    private final String name;

    protected Scope(String name)
    {
        this(null, name);
    }

    public Scope(Scope parent, String name)
    {
        this.parent = parent;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public Scope getParent()
    {
        return parent;
    }
}
