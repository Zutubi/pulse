package com.zutubi.pulse.core.engine.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Required;

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

    @Override @Required
    public String getName()
    {
        return super.getName();
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
