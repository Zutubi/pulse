package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.model.ActionLink;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a row in the agents table.
 */
public class AgentRowModel
{
    private Agent agent;
    private String name;
    private String location;
    private String status;
    private List<ActionLink> actions = new LinkedList<ActionLink>();
    private BuildResult executingBuild;
    private RecipeResultNode executingNode;

    public AgentRowModel(Agent agent, String name, String location, String status)
    {
        this.agent = agent;
        this.name = name;
        this.location = location;
        this.status = status;
    }

    public Agent getAgent()
    {
        return agent;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public BuildResult getExecutingBuild()
    {
        return executingBuild;
    }

    public RecipeResultNode getExecutingNode()
    {
        return executingNode;
    }

    public void setExecutingStage(BuildResult buildResult, RecipeResultNode node)
    {
        this.executingBuild = buildResult;
        this.executingNode = node;
    }

    public boolean hasActions()
    {
        return actions.size() > 0;
    }
    
    public List<ActionLink> getActions()
    {
        return actions;
    }

    public void addAction(ActionLink action)
    {
        actions.add(action);
    }
}
