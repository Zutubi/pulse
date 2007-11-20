package com.zutubi.pulse.web.agents;

import com.zutubi.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.LookupErrorException;

/**
 */
public class AgentActionBase extends ActionSupport
{
    private String agentName;
    private Agent agent;
    protected AgentManager agentManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public String getAgentName()
    {
        return agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public String getu_agentName()
    {
        return uriComponentEncode(agentName);
    }

    public String geth_agentName()
    {
        return htmlEncode(agentName);
    }

    public Agent getAgent()
    {
        if(agent == null)
        {
            if (TextUtils.stringSet(agentName))
            {
                agent = agentManager.getAgent(agentName);
                if(agent == null)
                {
                    throw new LookupErrorException("Unknown agent '" + agentName + "'");
                }
                if(!configurationTemplateManager.isDeeplyValid(agent.getConfig().getConfigurationPath()))
                {
                    throw new LookupErrorException("Agent configuration is invalid.");
                }
            }
        }

        return agent;
    }

    public Agent getRequiredAgent()
    {
        Agent agent = getAgent();
        if(agent == null)
        {
            throw new LookupErrorException("Agent name is required");
        }

        return agent;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
