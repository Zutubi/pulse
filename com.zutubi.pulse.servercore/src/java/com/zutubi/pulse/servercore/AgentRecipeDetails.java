package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.tove.variables.GenericVariable;
import com.zutubi.tove.variables.HashVariableMap;
import com.zutubi.tove.variables.api.VariableMap;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import static com.zutubi.util.io.FileSystemUtils.encodeFilenameComponent;

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
    private boolean update;
    private String projectPersistentPattern;
    private String projectTempPattern;

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
        setUpdate(context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BOOTSTRAP, false));
        setProjectPersistentPattern(context.getString(NAMESPACE_INTERNAL, PROPERTY_PERSISTENT_WORK_PATTERN));
        setProjectTempPattern(context.getString(NAMESPACE_INTERNAL, PROPERTY_TEMP_PATTERN));
    }
    
    public VariableMap createPathVariableMap()
    {
        VariableMap map = new HashVariableMap();
        map.add(new GenericVariable<String>(PROPERTY_AGENT, encodeFilenameComponent(agent)));
        map.add(new GenericVariable<String>(PROPERTY_AGENT_HANDLE, Long.toString(agentHandle)));
        map.add(new GenericVariable<String>(PROPERTY_PROJECT, encodeFilenameComponent(project)));
        map.add(new GenericVariable<String>(PROPERTY_PROJECT_HANDLE, Long.toString(projectHandle)));
        map.add(new GenericVariable<String>(PROPERTY_RECIPE_ID, Long.toString(recipeId)));
        map.add(new GenericVariable<String>(PROPERTY_STAGE, encodeFilenameComponent(stage)));
        map.add(new GenericVariable<String>(PROPERTY_STAGE_HANDLE, Long.toString(stageHandle)));
        return map;
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

    public boolean isUpdate()
    {
        return update;
    }

    public void setUpdate(boolean update)
    {
        this.update = update;
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

    public String getProjectTempPattern()
    {
        return projectTempPattern;
    }

    public void setProjectTempPattern(String projectTempPattern)
    {
        this.projectTempPattern = projectTempPattern;
    }
}
