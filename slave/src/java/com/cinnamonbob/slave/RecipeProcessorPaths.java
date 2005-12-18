package com.cinnamonbob.slave;

import com.cinnamonbob.bootstrap.ConfigurationManager;

import java.io.File;

/**
 */
public class RecipeProcessorPaths
{
    private ConfigurationManager configurationManager;
    private File buildRoot;

    public RecipeProcessorPaths(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
        init();
    }

    public RecipeProcessorPaths()
    {

    }

    public void init()
    {
        File systemRoot = configurationManager.getApplicationPaths().getSystemRoot();
        buildRoot = new File(systemRoot, "builds");
    }

    public File getWorkDir(long recipe)
    {
        return new File(getRecipeRoot(recipe), "work");
    }

    public File getWorkZip(long recipe)
    {
        return new File(getWorkDir(recipe).getAbsolutePath() + ".zip");
    }

    public File getOutputDir(long recipe)
    {
        return new File(getRecipeRoot(recipe), "output");
    }

    public File getOutputZip(long recipe)
    {
        return new File(getOutputDir(recipe).getAbsolutePath() + ".zip");
    }

    public File getRecipeRoot(long recipe)
    {
        return new File(buildRoot, String.format("%08d", recipe));
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
