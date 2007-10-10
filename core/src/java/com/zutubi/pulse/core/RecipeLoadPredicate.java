package com.zutubi.pulse.core;

import com.opensymphony.util.TextUtils;
import nu.xom.Element;

/**
 * A predicate to ensure only the recipe of the given name is loaded.
 */
public class RecipeLoadPredicate implements TypeLoadPredicate
{
    private PulseFile pulseFile;
    private String recipeName;

    public RecipeLoadPredicate(PulseFile pulseFile, String recipeName)
    {
        this.pulseFile = pulseFile;
        this.recipeName = recipeName;
    }

    public boolean loadType(Object type, Element element)
    {
        if(type instanceof Recipe)
        {
            if(!TextUtils.stringSet(recipeName))
            {
                recipeName = pulseFile.getDefaultRecipe();
            }

            if(!TextUtils.stringSet(recipeName))
            {
                return false;
            }
            else
            {
                Recipe recipe = (Recipe) type;
                return recipe.getName().equals(recipeName);
            }
        }

        return true;
    }

    public boolean resolveReferences(Object type, Element element)
    {
        return true;
    }

    public boolean allowUnresolved(Object type, Element element)
    {
        return false;
    }
}
