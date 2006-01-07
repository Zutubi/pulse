package com.cinnamonbob;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;

import java.io.File;

/**
 */
public class DefaultRecipeResultCollector implements RecipeResultCollector
{
    private Project project;
    private MasterBuildPaths paths = new MasterBuildPaths();

    public DefaultRecipeResultCollector(Project project)
    {
        this.project = project;
    }

    public void prepare(BuildResult result, long recipeId)
    {
        // ensure that we have created the necessary directories.
        File recipeDir = paths.getRecipeDir(project, result, recipeId);
        if (!recipeDir.mkdirs())
        {
            throw new BuildException("Failed to create the '" + recipeDir + "' directory.");
        }
    }

    public void collect(BuildResult result, long recipeId, BuildService buildService)
    {
        buildService.collectResults(recipeId, paths.getRecipeDir(project, result, recipeId));
    }

    public void cleanup(BuildResult result, long recipeId, BuildService buildService)
    {
        if (buildService != null)
        {
            buildService.cleanup(recipeId);
        }
    }

}
