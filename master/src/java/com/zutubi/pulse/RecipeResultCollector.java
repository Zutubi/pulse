package com.zutubi.pulse;

import com.zutubi.pulse.model.BuildResult;

/**
 */
public interface RecipeResultCollector
{
    void prepare(BuildResult result, long recipeId);

    void collect(BuildResult result, long recipeId, boolean collectWorkingCopy, BuildService buildService);

    void cleanup(BuildResult result, long recipeId, BuildService buildService);

}
