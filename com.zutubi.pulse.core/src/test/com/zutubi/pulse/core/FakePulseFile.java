package com.zutubi.pulse.core;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.LinkedList;
import java.util.List;

public class FakePulseFile
{
    private List<FakeRecipe> recipes = new LinkedList<FakeRecipe>();

    public void add(FakeRecipe recipe)
    {
        recipes.add(recipe);
    }

    public List<FakeRecipe> getRecipes()
    {
        return recipes;
    }

    public FakeRecipe getRecipe(final String name)
    {
        return CollectionUtils.find(recipes, new Predicate<FakeRecipe>()
        {
            public boolean satisfied(FakeRecipe recipe)
            {
                return recipe.getName().equals(name);
            }
        });
    }
}
