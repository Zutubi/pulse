package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.marshal.TypeLoadPredicate;
import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 * A predicate to prevent the bodies of recipes being loaded.
 */
public class RecipeListingPredicate implements TypeLoadPredicate
{
    public boolean loadType(Configuration type, Element element)
    {
        return !(type instanceof RecipeConfiguration);
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
