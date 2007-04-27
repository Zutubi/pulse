package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.model.BuildResult;

import java.io.File;

/**
 */
public class DefaultRecipeResultCollector implements RecipeResultCollector
{
    private MasterBuildPaths paths;

    public DefaultRecipeResultCollector(MasterConfigurationManager configManager)
    {
        this.paths = new MasterBuildPaths(configManager);
    }

    public void prepare(BuildResult result, long recipeId)
    {
        // ensure that we have created the necessary directories.
        File recipeDir = paths.getRecipeDir(result, recipeId);
        if (!recipeDir.mkdirs())
        {
            throw new BuildException("Failed to create the '" + recipeDir + "' directory.");
        }
    }

    public void collect(BuildResult result, long recipeId, boolean collectWorkingCopy, boolean incremental, AgentService agentService)
    {
        if (agentService != null)
        {
            File outputDest = paths.getOutputDir(result, recipeId);
            File workDest = null;
            if (collectWorkingCopy)
            {
                workDest = paths.getBaseDir(result, recipeId);
            }

            agentService.collectResults(result.getProject().getName(), result.getBuildSpecification(), recipeId, incremental, outputDest, workDest);
        }
    }

    public void cleanup(BuildResult result, long recipeId, boolean incremental, AgentService agentService)
    {
        if (agentService != null)
        {
            agentService.cleanup(result.getProject().getName(), result.getBuildSpecification(), recipeId, incremental);
        }
    }

    public File getRecipeDir(BuildResult result, long recipeId)
    {
        return paths.getRecipeDir(result, recipeId);
    }


}
