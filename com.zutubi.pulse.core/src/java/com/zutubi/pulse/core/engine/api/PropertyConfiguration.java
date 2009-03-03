package com.zutubi.pulse.core.engine.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * A simple string-valued reference.
 */
@SymbolicName("zutubi.property")
@Referenceable(valueProperty = "value")
public class PropertyConfiguration extends AbstractNamedConfiguration
{
    private String value;

    public PropertyConfiguration()
    {
    }

    public PropertyConfiguration(String name, String value)
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
}
