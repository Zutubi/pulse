package com.cinnamonbob;

import com.cinnamonbob.events.build.RecipeCommencedEvent;
import com.cinnamonbob.events.build.RecipeCompletedEvent;
import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.BuildSpecification;

public class BuildInvocation
{

    private BuildManager buildManager;

    public void startBuild(BuildSpecification spec)
    {

    }

    public void startRecipe(RecipeCommencedEvent recipeCommencedEvent)
    {

    }

    public void finishRecipe(RecipeCompletedEvent recipeCompletedEvent)
    {

    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}

