package com.zutubi.pulse;

import com.zutubi.pulse.events.build.RecipeCommencedEvent;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildSpecification;

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

