package com.cinnamonbob;

import com.cinnamonbob.model.BuildResult;

/**
 */
public interface RecipeResultCollector
{
    void prepare(BuildResult result, long recipeId);

    void collect(BuildResult result, long recipeId, BuildService buildService);

    void cleanup(BuildResult result, long recipeId, BuildService buildService);

}
