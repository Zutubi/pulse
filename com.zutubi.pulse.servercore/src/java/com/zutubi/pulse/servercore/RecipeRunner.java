package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;

/**
 * Interface for instances that can run recipes on a processor.  Allows
 * implementations to wrap setup/teardown activities around the recipe
 * processing, e.g. additional context setup.
 */
public interface RecipeRunner
{
    /**
     * Runs the given recipe using the given processor.
     *
     * @param request         request providing the recipe to run
     * @param recipeProcessor the processor to use for running the recipe
     */
    public void runRecipe(RecipeRequest request, RecipeProcessor recipeProcessor);
}
