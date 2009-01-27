package com.zutubi.pulse.core.engine.api;

import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * A base class to simplify references that are named objects where the
 * reference value is the object itself.
 * FIXME loader
 */
public class SelfReference extends AbstractNamedConfiguration implements Reference
{
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
        setName(name);
    }

    /**
     * @return this, as this is self-referential
     */
    public Object referenceValue()
    {
        return this;
    }
}
