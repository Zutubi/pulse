package com.zutubi.pulse.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class PulseFile
{
    private String defaultRecipe;
    private String name;

    private List<Recipe> recipes = new LinkedList<Recipe>();
    private List<Reference> references = new LinkedList<Reference>();

    public String getDefaultRecipe()
    {
        return defaultRecipe;
    }

    public void setDefaultRecipe(String defaultRecipe)
    {
        this.defaultRecipe = defaultRecipe;
    }

    public void addRecipe(Recipe r) throws FileLoadException
    {
        if(getRecipe(r.getName()) != null)
        {
            throw new FileLoadException("A recipe with name '" + r.getName() + "' already exists");
        }
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

    public void add(Reference r) throws FileLoadException
    {
        references.add(r);
    }

    public List<Reference> getReferences()
    {
        return Collections.unmodifiableList(references);
    }

    public Reference getReference(String name)
    {
        for (Reference reference: references)
        {
            if (name.equals(reference.getName()))
            {
                return reference;
            }
        }
        return null;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
