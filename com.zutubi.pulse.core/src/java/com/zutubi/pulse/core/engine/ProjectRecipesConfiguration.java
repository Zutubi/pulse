package com.zutubi.pulse.core.engine;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 */
@SymbolicName("zutubi.projectRecipes")
public class ProjectRecipesConfiguration extends AbstractConfiguration
{
    private String defaultRecipe;
    private Map<String, RecipeConfiguration> recipes = new HashMap<String, RecipeConfiguration>();

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
}
