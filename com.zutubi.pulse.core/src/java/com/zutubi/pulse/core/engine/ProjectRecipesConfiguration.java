package com.zutubi.pulse.core.engine;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds a collection of recipes from a Pulse file.  Used as the root when
 * loading Pulse files for further processing.
 */
@SymbolicName("zutubi.projectRecipes")
public class ProjectRecipesConfiguration extends AbstractConfiguration
{
    private String defaultRecipe;
    private Map<String, RecipeConfiguration> recipes = new LinkedHashMap<String, RecipeConfiguration>();

    public String getDefaultRecipe()
    {
        return defaultRecipe;
    }

    public void setDefaultRecipe(String defaultRecipe)
    {
        this.defaultRecipe = defaultRecipe;
    }

    public Map<String, RecipeConfiguration> getRecipes()
    {
        return recipes;
    }

    public void setRecipes(Map<String, RecipeConfiguration> recipes)
    {
        this.recipes = recipes;
    }

    public void addRecipe(RecipeConfiguration recipe)
    {
        recipes.put(recipe.getName(), recipe);
    }
}
