package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.marshal.TypeLoadPredicate;
import com.zutubi.util.TextUtils;
import nu.xom.Element;

/**
 * A predicate to ensure only the recipe of the given name is loaded.
 */
public class RecipeLoadPredicate implements TypeLoadPredicate
{
    private ProjectRecipesConfiguration recipesConfiguration;
    private String recipeName;

    public RecipeLoadPredicate(ProjectRecipesConfiguration recipesConfiguration, String recipeName)
    {
        this.recipesConfiguration = recipesConfiguration;
        this.recipeName = recipeName;
    }

    public boolean loadType(Object type, Element element)
    {
        if(type instanceof Recipe)
        {
            if(!TextUtils.stringSet(recipeName))
            {
                recipeName = recipesConfiguration.getDefaultRecipe();
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

    public boolean validate(Object type, Element element)
    {
        return loadType(type, element);
    }
}
