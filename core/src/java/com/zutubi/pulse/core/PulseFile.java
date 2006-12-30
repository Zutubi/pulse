package com.zutubi.pulse.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class PulseFile implements ScopeAware
{
    private String defaultRecipe;
    private String name;

    private List<Recipe> recipes = new LinkedList<Recipe>();
    private List<Dependency> dependencies = new LinkedList<Dependency>();
    private Scope globalScope;

    public String getDefaultRecipe()
    {
        return defaultRecipe;
    }

    public void setDefaultRecipe(String defaultRecipe)
    {
        this.defaultRecipe = defaultRecipe;
    }

    public Scope getGlobalScope()
    {
        return globalScope;
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

        for (Recipe recipe : recipes)
        {
            if (name.equals(recipe.getName()))
            {
                return recipe;
            }
        }
        return null;
    }

    public List<Dependency> getDependencies()
    {
        return Collections.unmodifiableList(dependencies);
    }

    public void addDependency(Dependency dependency)
    {
        dependencies.add(dependency);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setScope(Scope scope)
    {
        this.globalScope = scope;
    }
}
