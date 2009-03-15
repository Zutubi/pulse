package com.zutubi.pulse.core.marshal.types;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

@SymbolicName("fakePulseFile")
public class FakePulseFile extends AbstractConfiguration
{
    @Addable("recipe") @Ordered
    private Map<String, FakeRecipe> recipes = new LinkedHashMap<String, FakeRecipe>();

    public Map<String, FakeRecipe> getRecipes()
    {
        return recipes;
    }

    public void setRecipes(Map<String, FakeRecipe> recipes)
    {
        this.recipes = recipes;
    }

    public FakeRecipe getRecipe(String name)
    {
        return recipes.get(name);
    }
}
