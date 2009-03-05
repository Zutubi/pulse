package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * A config type that makes bad references from its properties, to test error
 * handling.
 */
@SymbolicName("badReferences")
public class BadReferencesConfiguration extends AbstractConfiguration
{
    @Reference
    private UnreferenceableConfiguration unreferenceable;
    @Reference
    private BadReferenceNameConfiguration badReferenceName;

    public UnreferenceableConfiguration getUnreferenceable()
    {
        return unreferenceable;
    }

    public void setUnreferenceable(UnreferenceableConfiguration unreferenceable)
    {
        this.unreferenceable = unreferenceable;
    }

    public BadReferenceNameConfiguration getBadReferenceName()
    {
        return badReferenceName;
    }

    public void setBadReferenceName(BadReferenceNameConfiguration badReferenceName)
    {
        this.badReferenceName = badReferenceName;
    }
}
