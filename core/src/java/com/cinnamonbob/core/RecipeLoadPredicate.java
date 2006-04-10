package com.zutubi.pulse.core;

import nu.xom.Element;

/**
 * A predicate to ensure only the recipe of the given name is loaded.
 */
public class RecipeLoadPredicate implements TypeLoadPredicate
{
    private BobFile bobFile;
    private String recipeName;

    public RecipeLoadPredicate(BobFile bobFile, String recipeName)
    {
        this.bobFile = bobFile;
        this.recipeName = recipeName;
    }

    public boolean loadType(Object type, Element element)
    {
        if(type instanceof Recipe)
        {
            if(recipeName == null)
            {
                recipeName = bobFile.getDefaultRecipe();
            }

            if(recipeName == null)
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
}
