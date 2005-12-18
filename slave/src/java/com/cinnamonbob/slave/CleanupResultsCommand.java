package com.cinnamonbob.slave;

import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.File;

/**
 */
public class CleanupResultsCommand implements Runnable
{
    private long recipeId;
    private RecipeProcessorPaths recipeProcessorPaths;

    public CleanupResultsCommand(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public void run()
    {
        FileSystemUtils.removeDirectory(recipeProcessorPaths.getOutputDir(recipeId));
        File workZip = recipeProcessorPaths.getOutputZip(recipeId);
        workZip.delete();
    }

    public void setRecipeProcessorPaths(RecipeProcessorPaths recipeProcessorPaths)
    {
        this.recipeProcessorPaths = recipeProcessorPaths;
    }
}
