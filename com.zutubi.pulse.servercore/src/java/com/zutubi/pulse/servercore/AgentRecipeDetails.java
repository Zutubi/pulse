package com.zutubi.pulse.servercore;

/**
 * Holding class for various pieces of data identifying a recipe and its
 * results on an agent.  Used in lieu of a context as the context is not
 * available at all times when these details are needed.
 */
public class AgentRecipeDetails
{
    private long agentHandle;
    private String agent;
    private String agentDataPattern;
    private long projectHandle;
    private String project;
    private long recipeId;
    private boolean incremental;
    private String projectPersistentPattern;

    public AgentRecipeDetails(long agentHandle, String agent, String agentDataPattern, long projectHandle, String project, long recipeId, boolean incremental, String projectPersistentPattern)
    {
        this.agentHandle = agentHandle;
        this.agent = agent;
        this.agentDataPattern = agentDataPattern;
        this.projectHandle = projectHandle;
        this.project = project;
        this.recipeId = recipeId;
        this.incremental = incremental;
        this.projectPersistentPattern = projectPersistentPattern;
    }

    public long getAgentHandle()
    {
        return agentHandle;
    }

    public String getAgent()
    {
        return agent;
    }

    public String getAgentDataPattern()
    {
        return agentDataPattern;
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

    public String getProjectPersistentPattern()
    {
        return projectPersistentPattern;
    }
}
