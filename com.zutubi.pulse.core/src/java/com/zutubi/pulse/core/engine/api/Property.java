package com.zutubi.pulse.core.engine.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * A simple string-valued reference.
 */
@SymbolicName("zutubi.property")
public class Property extends AbstractNamedConfiguration implements Reference
{
    private String value;

    public Property()
    {
    }

    public Property(String name, String value)
    {
        setName(name);
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String referenceValue()
    {
        return value;
    }
}
