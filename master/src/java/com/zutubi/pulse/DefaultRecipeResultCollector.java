package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

import java.io.File;

/**
 */
public class DefaultRecipeResultCollector implements RecipeResultCollector
{
    private Project project;
    private MasterBuildPaths paths;

    public DefaultRecipeResultCollector(Project project, MasterConfigurationManager configManager)
    {
        this.project = project;
        this.paths = new MasterBuildPaths(configManager);
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
        if (buildService != null)
        {
            File outputDest = paths.getOutputDir(project, result, recipeId);
            File workDest = paths.getBaseDir(project, result, recipeId);

            if (!outputDest.mkdirs())
            {
                throw new BuildException("Unable to create output destination '" + outputDest.getAbsolutePath() + "'");
            }

            if (!workDest.mkdirs())
            {
                throw new BuildException("Unable to create work destination '" + workDest.getAbsolutePath() + "'");
            }

            buildService.collectResults(recipeId, outputDest, workDest);
        }
    }

    public void cleanup(BuildResult result, long recipeId, BuildService buildService)
    {
        if (buildService != null)
        {
            buildService.cleanup(recipeId);
        }
    }


}
