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
    public void runRecipe(RecipeRequest request, RecipeProcessor recipeProcessor);
}
