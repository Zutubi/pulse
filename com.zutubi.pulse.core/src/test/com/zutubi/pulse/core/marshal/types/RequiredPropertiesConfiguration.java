package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * A type with some required properties.
 */
@SymbolicName("requiredProperties")
public class RequiredPropertiesConfiguration extends AbstractNamedConfiguration
{
    @Required
    private String requiredString;
    @Required @Reference
    private TrivialConfiguration requiredReference;
    @Required
    private TrivialConfiguration requiredComposite;

    public String getRequiredString()
    {
        return requiredString;
    }

    public void setRequiredString(String requiredString)
    {
        this.requiredString = requiredString;
    }

    public TrivialConfiguration getRequiredReference()
    {
        return requiredReference;
    }

    public void setRequiredReference(TrivialConfiguration requiredReference)
    {
        this.requiredReference = requiredReference;
    }

    public TrivialConfiguration getRequiredComposite()
    {
        return requiredComposite;
    }

    public void setRequiredComposite(TrivialConfiguration requiredComposite)
    {
        this.requiredComposite = requiredComposite;
    }
}
