package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.BuildException;

import java.io.File;

/**
 * <class-comment/>
 */
public class MasterBuildService implements BuildService
{
    private MasterRecipeProcessor masterRecipeProcessor;
    private ConfigurationManager configurationManager;

    public void build(RecipeRequest request)
    {
        // TODO the dodgy wiring goes on unabated!
        ComponentContext.autowire(this);
        masterRecipeProcessor.processRecipe(request);
    }

    public void collectResults(long recipeId, File dir)
    {
        if (!dir.delete())
        {
            throw new BuildException("Unable to remove directory '" + dir.getAbsolutePath() + "'");
        }

        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeId, configurationManager);
        File outputDir = recipePaths.getOutputDir();

        if (!outputDir.renameTo(dir))
        {
            throw new BuildException("Unable to rename output directory '" + outputDir.getAbsolutePath() + "' to '" + dir.getAbsolutePath() + "'");
        }
    }

    public void cleanupResults(long recipeId)
    {
        // We rename the output dir, so no need to remove it.
    }

    public void setMasterRecipeProcessor(MasterRecipeProcessor masterRecipeProcessor)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
