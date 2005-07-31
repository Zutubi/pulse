package com.cinnamonbob.core2.config;

import com.cinnamonbob.BobException;

import java.util.*;

/**
 *
 */
public class BobFile
{
    private String defaultRecipe;
    
    private Map<String, String> properties = new HashMap<String, String>();
    
    private Map<String, Reference> reference = new HashMap<String, Reference>();
            
    private List<Recipe> recipes = new LinkedList<Recipe>();
    
    public void addProperties(Map<String, String> props)
    {
        properties.putAll(props);
    }
    
    public void setProperty(String name, String value)
    {
        properties.put(name, value);
    }

    public String getProperty(String name)
    {
        return properties.get(name);
    }
    
    public void setReference(String name, Reference ref)
    {
        reference.put(name, ref);
    }
    
    public Reference getReference(String name)
    {
        return reference.get(name);
    }

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
        
    /**
     * @param input
     * @return
     * @throws com.cinnamonbob.BobException
     */
    public String replaceVariables(String input) throws BobException
    {
        return VariableHelper.replaceVariables(input, properties);
    }
}
