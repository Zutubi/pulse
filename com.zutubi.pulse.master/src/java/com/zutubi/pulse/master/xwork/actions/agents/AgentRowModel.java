package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.tove.model.ActionLink;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a row in the agents table.
 */
public class AgentRowModel
{
    private long id;
    private String name;
    private String location;
    private String status;
    private ExecutingStageModel executingStage;
    private List<ActionLink> actions = new LinkedList<ActionLink>();

    public AgentRowModel(long id, String name, String location, String status)
    {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getLocation()
    {
        return location;
    }

    public String getStatus()
    {
        return status;
    }

    public ExecutingStageModel getExecutingStage()
    {
        return executingStage;
    }

    public void setExecutingStage(ExecutingStageModel executingStage)
    {
        this.executingStage = executingStage;
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
