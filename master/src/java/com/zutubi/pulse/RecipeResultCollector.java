package com.zutubi.pulse;

import com.zutubi.pulse.model.BuildResult;

import java.io.File;

/**
 */
public interface RecipeResultCollector
{
    void prepare(BuildResult result, long recipeId);

    void collect(BuildResult result, long recipeId, boolean collectWorkingCopy, boolean incremental, BuildService buildService);

    void cleanup(BuildResult result, long recipeId, boolean incremental, BuildService buildService);

    File getRecipeDir(BuildResult result, long recipeId);
}
