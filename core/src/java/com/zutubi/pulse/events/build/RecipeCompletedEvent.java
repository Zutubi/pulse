package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.model.RecipeResult;

/**
 * This event is raised by the recipe processor when a recipe is completed.
 */
public class RecipeCompletedEvent extends RecipeEvent
{
    private RecipeResult result;
    /**
     * If non-null, the build version as extracted when running this recipe.
     */
    private String buildVersion;

    private RecipeCompletedEvent()
    {
        // For hessian
        super(null, 0);
    }

    public RecipeCompletedEvent(Object source, RecipeResult result)
    {
        super(source, result.getId());
        this.result = result;
    }

    public RecipeResult getResult()
    {
        return result;
    }

    public String getBuildVersion()
    {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion)
    {
        this.buildVersion = buildVersion;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Completed Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
