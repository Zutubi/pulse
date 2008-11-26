package com.zutubi.pulse.core.engine.api;

/**
 * A base class to simplify references that are named objects where the
 * reference value is the object itself.
 */
public class SelfReference implements Reference
{
    private String name;

    /**
     * Creates a self-reference without a name (it must later be set).
     */
    public SelfReference()
    {
    }

    /**
     * Creates a self-reference with the given name.
     *
     * @param name the name of this reference
     */
    public SelfReference(String name)
    {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return name;
    }

    /**
     * Updates the name of this reference.
     *
     * @param name the new name of this reference
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return this, as this is self-referential
     */
    public Object getValue()
    {
        return this;
    }
}
