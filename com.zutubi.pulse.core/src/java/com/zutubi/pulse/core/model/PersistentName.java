package com.zutubi.pulse.core.model;

/**
 * An entity used to store a name.  It may seem strange to store a name by
 * itself, but the purpose is to allow the name to outlive the entity that
 * it is naming.
 */
public class PersistentName extends Entity
{
    private String name;

    public PersistentName()
    {
    }

    public PersistentName(String name)
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
}
