package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.RecipeResult;

/**
 */
public class BuildStage
{
    private BuildHostRequirements hostRequirements;
    private String recipe;

    public BuildStage()
    {
    }

    public BuildStage(BuildHostRequirements hostRequirements, String recipe)
    {
        this.hostRequirements = hostRequirements;
        this.recipe = recipe;
    }

    public BuildStage copy()
    {
        BuildStage copy = new BuildStage();
        copy.hostRequirements = hostRequirements.copy();
        copy.recipe = recipe;
        return copy;
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
