package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.TypeLoadPredicate;
import nu.xom.Element;

/**
 * A predicate used when validating the pulse file for a custom project.  We
 * allow unresolved references.
 */
public class CustomProjectValidationPredicate implements TypeLoadPredicate
{
    public boolean loadType(Object type, Element element)
    {
        return true;
    }

    public boolean resolveReferences(Object type, Element element)
    {
        return true;
    }

    public boolean allowUnresolved(Object type, Element element)
    {
        return true;
    }

    public boolean validate(Object type, Element element)
    {
        return loadType(type, element);
    }
}
