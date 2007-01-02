package com.zutubi.pulse.core;

import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.core.model.TestSuiteResult;

import java.io.File;
import java.io.OutputStream;

/**
 * Information passed to all commands as they execute, allowing the command
 * access to it's execution context (or environment).
 *
 * Depending upon the commands execution context, some of the context data may
 * or may not be available.  Full details are provided in each properties
 * javadoc.
 */
public class CommandContext
{
    private RecipeContext recipeContext;

    private File outputDir;

    public CommandContext()
    {
    }

    public void setRecipeContext(RecipeContext recipeContext)
    {
        this.recipeContext = recipeContext;
    }

    public RecipePaths getPaths()
    {
        return recipeContext.getPaths();
    }

    public File getOutputDir()
    {
        return outputDir;
    }

    public void setOutputDir(File outputDir)
    {
        this.outputDir = outputDir;
    }

    public TestSuiteResult getTestResults()
    {
        return recipeContext.getTestResults();
    }

    /**
     * The output stream to which the command should send the commands raw
     * output and error streams.
     *
     * This may be null.
     *
     * @return the output stream.
     */
    public OutputStream getOutputStream()
    {
        return recipeContext.getOutputStream();
    }

    /**
     * @return context for the containing build, if any (null if running a
     * standalone recipe)
     */
    public BuildContext getBuildContext()
    {
        return recipeContext.getBuildContext();
    }

    /**
     * Get the id of the recipe to which this command execution is associated.
     *
     * @return
     */
    public long getRecipeId()
    {
        return recipeContext.getRecipeId();
    }

    public long getRecipeStartTime()
    {
        return recipeContext.getRecipeStartTime();
    }

    public Scope getGlobalScope()
    {
        return recipeContext.getGlobalScope();
    }

    public void setRecipePaths(RecipePaths mungedPaths)
    {
        recipeContext.setRecipePaths(mungedPaths);
    }
}
