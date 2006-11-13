package com.zutubi.pulse.core;

import nu.xom.Element;

/**
 * A predicate to prevent the bodies of recipes being loaded.
 */
public class RecipeListingPredicate implements TypeLoadPredicate
{
    public boolean loadType(Object type, Element element)
    {
        return !(type instanceof Recipe);
    }

    public boolean resolveReferences(Object type, Element element)
    {
        return true;
    }
}
