package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.tove.model.ActionLink;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a row in the agents table.
 */
public class AgentModel
{
    private Agent agent;
    private String name;
    private String location;
    private String status;
    private List<ActionLink> actions = new LinkedList<ActionLink>();

    public AgentModel(Agent agent, String name, String location, String status)
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
