package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.model.AgentStateManager;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.LookupErrorException;
import com.opensymphony.util.TextUtils;

/**
 */
public class AgentActionSupport extends ActionSupport
{
    public static String AGENT_ERROR = "agenterror";

    private long agentHandle;
    private String agentName;
    private Agent agent;
    private AgentManager agentManager;

    public void setAgentHandle(long agentHandle)
    {
        this.agentHandle = agentHandle;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public Agent getAgent()
    {
        if(agent == null)
        {
            if(TextUtils.stringSet(agentName))
            {
                agent = agentManager.getAgent(agentName);
                if(agent == null)
                {
                    throw new LookupErrorException("Unknown agent [" + agentName + "]");
                }
            }
            else
            {
                agent = agentManager.getAgent(agentHandle);
                if(agent == null)
                {
                    throw new LookupErrorException("Unknown agent [" + agentHandle + "]");
                }
            }

        }
        return agent;
    }

    public AgentManager getAgentManager()
    {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
