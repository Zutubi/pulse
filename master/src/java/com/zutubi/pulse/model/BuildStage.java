package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.PersistentName;
import com.zutubi.pulse.core.model.RecipeResult;

/**
 */
public class BuildStage
{
    private PersistentName pname;
    private BuildHostRequirements hostRequirements;
    private String recipe;

    public BuildStage()
    {
    }

    public BuildStage(String name, BuildHostRequirements hostRequirements, String recipe)
    {
        this.pname = new PersistentName(name);
        this.hostRequirements = hostRequirements;
        this.recipe = recipe;
    }

    public BuildStage copy()
    {
        BuildStage copy = new BuildStage();
        copy.pname = new PersistentName(pname.getName());
        if (hostRequirements != null)
        {
            copy.hostRequirements = hostRequirements.copy();
        }
        copy.recipe = recipe;
        return copy;
    }

    public String getName()
    {
        if(pname == null)
        {
            return null;
        }
        else
        {
            return pname.getName();
        }
    }

    public void setName(String name)
    {
        if(pname == null)
        {
            pname = new PersistentName(name);
        }
        else
        {
            pname.setName(name);
        }
    }

    public PersistentName getPname()
    {
        return pname;
    }

    public void setPname(PersistentName pname)
    {
        this.pname = pname;
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
