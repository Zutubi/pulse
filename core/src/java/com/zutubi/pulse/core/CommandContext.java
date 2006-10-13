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
    /**
     * Paths for the recipe being executed.
     */
    private RecipePaths paths;
    /**
     * Output directory for the command being executed.
     */
    private File outputDir;
    /**
     * Test results being accumulated for the recipe.
     */
    private TestSuiteResult testResults;
    /**
     * If not null, stream to write command output to (in addition to any output artifact).
     */
    private OutputStream outputStream;

    private long recipeId;

    private BuildContext buildContext;

    public CommandContext(RecipePaths paths, File outputDir, TestSuiteResult testResults)
    {
        this.paths = paths;
        this.outputDir = outputDir;
        this.testResults = testResults;
    }

    public RecipePaths getPaths()
    {
        return paths;
    }

    public File getOutputDir()
    {
        return outputDir;
    }

    public TestSuiteResult getTestResults()
    {
        return testResults;
    }

    /**
     * The output stream to which the command should send the commands raw
     * output and error streams.
     *
     * This may be null.
     *
     * @return
     */
    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

    /**
     * @return context for the containing build, if any (null if running a
     * standalone recipe)
     */
    public BuildContext getBuildContext()
    {
        return buildContext;
    }

    public void setBuildContext(BuildContext buildContext)
    {
        this.buildContext = buildContext;
    }

    public long getBuildNumber()
    {
        if(buildContext == null)
        {
            return -1;
        }
        else
        {
            return buildContext.getBuildNumber();
        }
    }
    
    /**
     * Get the id of the recipe to which this command execution is associated.
     *
     * @return
     */
    public long getRecipeId()
    {
        return recipeId;
    }

    public void setRecipeId(long recipeId)
    {
        this.recipeId = recipeId;
    }
}
