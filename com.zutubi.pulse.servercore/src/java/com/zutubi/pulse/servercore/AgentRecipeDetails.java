package com.zutubi.pulse.servercore;

/**
 * Holding class for various pieces of data identifying a recipe and its
 * results on an agent.  Used in lieu of a context as the context is not
 * available at all times when these details are needed.
 */
public class AgentRecipeDetails
{
    private long projectHandle;
    private String project;
    private long recipeId;
    private boolean incremental;
    private String persistentPattern;

    public AgentRecipeDetails(long projectHandle, String project, long recipeId, boolean incremental, String persistentPattern)
    {
        this.projectHandle = projectHandle;
        this.project = project;
        this.recipeId = recipeId;
        this.incremental = incremental;
        this.persistentPattern = persistentPattern;
    }

    public long getProjectHandle()
    {
        return projectHandle;
    }

    public String getProject()
    {
        return project;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    public boolean isIncremental()
    {
        return incremental;
    }

    public String getPersistentPattern()
    {
        return persistentPattern;
    }
}
