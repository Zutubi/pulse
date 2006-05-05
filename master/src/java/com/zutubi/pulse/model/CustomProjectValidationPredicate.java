package com.zutubi.pulse.model;

import com.zutubi.pulse.core.TypeLoadPredicate;
import com.zutubi.pulse.core.ResourceReference;
import nu.xom.Element;

/**
 * A predicate used when validating the pulse file for a custom project.  We
 * don't resolve references and don't load resource references.
 */
public class CustomProjectValidationPredicate implements TypeLoadPredicate
{
    public boolean loadType(Object type, Element element)
    {
        return !(type instanceof ResourceReference);
    }

    public boolean resolveReferences(Object type, Element element)
    {
        return false;
    }
}
