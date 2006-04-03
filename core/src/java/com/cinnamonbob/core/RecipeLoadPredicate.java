package com.cinnamonbob.core;

import nu.xom.Element;

/**
 * A predicate to ensure only the recipe of the given name is loaded.
 */
public class RecipeLoadPredicate implements TypeLoadPredicate
{
    private String recipeName;

    public RecipeLoadPredicate(BobFile bobFile, String recipeName)
    {
        if(recipeName == null)
        {
            this.recipeName = bobFile.getDefaultRecipe();
        }
        else
        {
            this.recipeName = recipeName;
        }
    }

    public boolean loadType(Object type, Element element)
    {
        if(type instanceof Recipe)
        {
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
