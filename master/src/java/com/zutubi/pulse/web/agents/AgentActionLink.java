package com.zutubi.pulse.web.agents;

/**
 * Represents an action link in the agents table.
 */
public class AgentActionLink
{
    private String action;
    private String label;

    public AgentActionLink(String action, String label)
    {
        this.action = action;
        this.label = label;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }
}
