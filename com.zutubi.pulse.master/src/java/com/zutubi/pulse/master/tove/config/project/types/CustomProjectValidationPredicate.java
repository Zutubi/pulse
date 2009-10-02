package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.marshal.TypeLoadPredicate;
import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 * A predicate used when validating the pulse file for a custom project.  We
 * allow unresolved references.
 */
public class CustomProjectValidationPredicate implements TypeLoadPredicate
{
    public boolean loadType(Configuration type, Element element)
    {
        return true;
    }

    public boolean allowUnresolved(Configuration type, Element element)
    {
        return true;
    }

    public boolean validate(Configuration type, Element element)
    {
        return loadType(type, element);
    }
}
