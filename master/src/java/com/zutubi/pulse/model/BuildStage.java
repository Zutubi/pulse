package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.RecipeResult;

/**
 */
public class BuildStage
{
    private String name;
    private BuildHostRequirements hostRequirements;
    private String recipe;

    public BuildStage()
    {
    }

    public BuildStage(String name, BuildHostRequirements hostRequirements, String recipe)
    {
        this.name = name;
        this.hostRequirements = hostRequirements;
        this.recipe = recipe;
    }

    public BuildStage copy()
    {
        BuildStage copy = new BuildStage();
        copy.name = name;
        copy.hostRequirements = hostRequirements.copy();
        copy.recipe = recipe;
        return copy;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public BuildHostRequirements getHostRequirements()
    {
        return hostRequirements;
    }

    public void setHostRequirements(BuildHostRequirements hostRequirements)
    {
        this.hostRequirements = hostRequirements;
    }

    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public String getRecipeSafe()
    {
        return RecipeResult.getRecipeSafe(recipe);
    }

}
