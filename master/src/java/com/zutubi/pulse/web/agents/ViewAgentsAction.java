package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.Agent;

import java.util.List;

/**
 * An action to display all agents attached to this master, including the
 * master itself.
 */
public class ViewAgentsAction extends ActionSupport
{
    private List<Agent> agents;
    private ConfigurationManager configurationManager;
    private AgentManager agentManager;

    public List<Agent> getAgents()
    {
        return agents;
    }

    public int getServerPort()
    {
        return configurationManager.getAppConfig().getServerPort();
    }

    public String execute() throws Exception
    {
        // Update the statuses
        agentManager.pingSlaves();
        agents = agentManager.getAllAgents();
        return SUCCESS;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
