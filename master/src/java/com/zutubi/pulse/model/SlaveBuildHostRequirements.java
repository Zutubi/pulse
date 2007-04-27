package com.zutubi.pulse.model;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.SlaveAgentService;
import com.zutubi.pulse.RecipeDispatchRequest;

/**
 */
public class SlaveBuildHostRequirements extends AbstractBuildHostRequirements
{
    private AgentState agentState;

    public SlaveBuildHostRequirements()
    {

    }

    public SlaveBuildHostRequirements(AgentState agentState)
    {
        this.agentState = agentState;
    }

    public SlaveBuildHostRequirements copy()
    {
        // Don't deep copy the slave reference!
        return new SlaveBuildHostRequirements(agentState);
    }

    public boolean fulfilledBy(RecipeDispatchRequest request, AgentService service)
    {
        if (service instanceof SlaveAgentService)
        {
            SlaveAgentService slaveService = (SlaveAgentService) service;
            return slaveService.getAgentConfig().getAgentId() == agentState.getId();
        }

        return false;
    }

    public String getSummary()
    {
        // FIXME this will be easily fixed when host requirements are part of
        // FIXME the config subsystem
        return "foo";//agentState.getName();
    }

    public AgentState getSlave()
    {
        return agentState;
    }

    private void setSlave(AgentState agentState)
    {
        this.agentState = agentState;
    }
}
