package com.cinnamonbob;

import java.io.File;

/**
 * <class-comment/>
 */
public class MasterBuildService implements BuildService
{
    private long currentRecipe;

    public void build(RecipeRequest request)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void collectResults(long recipeId, File dir)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cleanupResults(long recipeId)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAvailable()
    {
        return currentRecipe == -1;
    }
}
