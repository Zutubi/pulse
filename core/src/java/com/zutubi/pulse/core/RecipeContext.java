package com.zutubi.pulse.core;

import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.core.model.TestSuiteResult;

import java.io.OutputStream;

/**
 * <class comment/>
 */
public class RecipeContext
{
    /**
     * Paths for the recipe being executed.
     */
    private RecipePaths paths;

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

    private Scope globalScope;

    private long recipeStartTime = -1;

    public RecipeContext()
    {
    }

    public RecipePaths getPaths()
    {
        return paths;
    }

    public void setRecipePaths(RecipePaths recipePaths)
    {
        this.paths = recipePaths;
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

    /**
     * Get the id of the recipe to which this command execution is associated.
     *
     * @return recipe id.
     */
    public long getRecipeId()
    {
        return recipeId;
    }

    public void setRecipeId(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public long getRecipeStartTime()
    {
        return recipeStartTime;
    }

    public void setRecipeStartTime(long recipeStartTime)
    {
        this.recipeStartTime = recipeStartTime;
    }

    public Scope getGlobalScope()
    {
        return globalScope;
    }

    public void setGlobalScope(Scope globalScope)
    {
        this.globalScope = globalScope;
    }

    public TestSuiteResult getTestResults()
    {
        return testResults;
    }

    public void setTestResults(TestSuiteResult testResults)
    {
        this.testResults = testResults;
    }
}
