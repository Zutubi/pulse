package com.zutubi.pulse.servercore;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;

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
    private long stageHandle;
    private String stage;
    private long recipeId;
    private boolean incremental;
    private String projectPersistentPattern;

    public AgentRecipeDetails()
    {
    }

    public AgentRecipeDetails(ExecutionContext context)
    {
        setAgentHandle(context.getLong(NAMESPACE_INTERNAL, PROPERTY_AGENT_HANDLE, 0));
        setAgent(context.getString(NAMESPACE_INTERNAL, PROPERTY_AGENT));
        setAgentDataPattern(context.getString(NAMESPACE_INTERNAL, PROPERTY_AGENT_DATA_PATTERN));
        setProjectHandle(context.getLong(NAMESPACE_INTERNAL, PROPERTY_PROJECT_HANDLE, 0));
        setProject(context.getString(NAMESPACE_INTERNAL, PROPERTY_PROJECT));
        setStageHandle(context.getLong(NAMESPACE_INTERNAL, PROPERTY_STAGE_HANDLE, 0));
        setStage(context.getString(NAMESPACE_INTERNAL, PROPERTY_STAGE));
        setRecipeId(context.getLong(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, 0));
        setIncremental(context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BUILD, false));
        setProjectPersistentPattern(context.getString(NAMESPACE_INTERNAL, PROPERTY_PERSISTENT_WORK_PATTERN));
    }

    public long getAgentHandle()
    {
        return agentHandle;
    }

    public void setAgentHandle(long agentHandle)
    {
        this.agentHandle = agentHandle;
    }

    public String getAgent()
    {
        return agent;
    }

    public void setAgent(String agent)
    {
        this.agent = agent;
    }

    public String getAgentDataPattern()
    {
        return agentDataPattern;
    }

    public void setAgentDataPattern(String agentDataPattern)
    {
        this.agentDataPattern = agentDataPattern;
    }

    public long getProjectHandle()
    {
        return projectHandle;
    }

    public void setProjectHandle(long projectHandle)
    {
        this.projectHandle = projectHandle;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public long getStageHandle()
    {
        return stageHandle;
    }

    public void setStageHandle(long stageHandle)
    {
        this.stageHandle = stageHandle;
    }

    public String getStage()
    {
        return stage;
    }

    public void setStage(String stage)
    {
        this.stage = stage;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    public void setRecipeId(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public boolean isIncremental()
    {
        return incremental;
    }

    public void setIncremental(boolean incremental)
    {
        this.incremental = incremental;
    }

    public String getProjectPersistentPattern()
    {
        return projectPersistentPattern;
    }

    public void setProjectPersistentPattern(String projectPersistentPattern)
    {
        this.projectPersistentPattern = projectPersistentPattern;
    }
}
