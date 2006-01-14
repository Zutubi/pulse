package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;

/**
 * <class-comment/>
 */
public class MasterBuildService implements BuildService
{
    private static final Logger LOG = Logger.getLogger(MasterBuildService.class);

    private MasterRecipeProcessor masterRecipeProcessor;
    private ConfigurationManager configurationManager;

    public String getUrl()
    {
        return BobServer.getHostURL();
    }

    public void build(RecipeRequest request)
    {
        // TODO the dodgy wiring goes on unabated!
        ComponentContext.autowire(this);
        masterRecipeProcessor.processRecipe(request);
    }

    public void collectResults(long recipeId, File dir)
    {
        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeId, configurationManager);
        File outputDir = recipePaths.getOutputDir();

        if (!FileSystemUtils.rename(outputDir, dir, true))
        {
            throw new BuildException("Unable to rename output directory '" + outputDir.getAbsolutePath() + "' to '" + dir.getAbsolutePath() + "'");
        }
    }

    public void cleanup(long recipeId)
    {
        // We rename the output dir, so no need to remove it.
        ServerRecipePaths recipePaths = new ServerRecipePaths(recipeId, configurationManager);
        File recipeRoot = recipePaths.getRecipeRoot();

        if (!FileSystemUtils.removeDirectory(recipeRoot))
        {
            throw new BuildException("Unable to remove recipe directory '" + recipeRoot.getAbsolutePath() + "'");
        }
    }

    public String getHostName()
    {
        return "[master]";
    }

    public void setMasterRecipeProcessor(MasterRecipeProcessor masterRecipeProcessor)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof MasterBuildService;
    }
}
