package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.marshal.TypeLoadPredicate;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.StringUtils;
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

    public boolean loadType(Configuration type, Element element)
    {
        if(type instanceof RecipeConfiguration)
        {
            if(!StringUtils.stringSet(recipeName))
            {
                recipeName = recipesConfiguration.getDefaultRecipe();
            }

            if(!StringUtils.stringSet(recipeName))
            {
                return false;
            }
            else
            {
                RecipeConfiguration recipe = (RecipeConfiguration) type;
                return recipe.getName().equals(recipeName);
            }
        }

        return true;
    }

    public boolean resolveReferences(Configuration type, Element element)
    {
        return true;
    }

    public boolean allowUnresolved(Configuration type, Element element)
    {
        return false;
    }

    public boolean validate(Configuration type, Element element)
    {
        return loadType(type, element);
    }
}
