package com.cinnamonbob.core;

import com.cinnamonbob.BobException;

import java.util.*;

/**
 *
 */
public class BobFile implements Namespace
{
    private String defaultRecipe;
    
    private List<Recipe> recipes = new LinkedList<Recipe>();
    
    public String getDefaultRecipe()
    {
        return defaultRecipe;
    }

    public void setDefaultRecipe(String defaultRecipe)
    {
        this.defaultRecipe = defaultRecipe;
    }
    
    public void addRecipe(Recipe r)
    {
        recipes.add(r);
    }
    
    public List<Recipe> getRecipes()
    {
        return Collections.unmodifiableList(recipes);
    }

    public Recipe getRecipe(String name)
    {
        if (name == null)
        {
            return null;    
        }
            
        for (Recipe recipe: recipes)
        {
            if (name.equals(recipe.getName()))
            {
                return recipe;
            }
        }
        return null;
    }
}
