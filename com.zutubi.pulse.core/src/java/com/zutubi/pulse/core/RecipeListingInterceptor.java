package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.marshal.ToveFileLoadInterceptor;
import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 * A predicate to prevent the bodies of recipes being loaded.
 */
public class RecipeListingInterceptor implements ToveFileLoadInterceptor
{
    public boolean loadInstance(Configuration instance, Element element, Scope scope)
    {
        return isNotRecipe(instance);
    }

    public boolean allowUnresolved(Configuration instance, Element element)
    {
        return true;
    }

    public boolean validate(Configuration instance, Element element)
    {
        return isNotRecipe(instance);
    }

    private boolean isNotRecipe(Configuration instance)
    {
        return !(instance instanceof RecipeConfiguration);
    }
}
