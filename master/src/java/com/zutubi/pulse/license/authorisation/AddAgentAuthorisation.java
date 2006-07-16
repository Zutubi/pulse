package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.license.License;

/**
 * <class-comment/>
 */
public class AddAgentAuthorisation implements Authorisation
{
    private AgentManager agentManager;

    public static final String[] AUTH = {"canAddAgent"};

    public String[] getAuthorisation(License license)
    {
        if (license == null)
        {
            return new String[0];
        }
        
        if (license.getSupportedAgents() == License.UNRESTRICTED)
        {
            return AUTH;
        }

        if (agentManager.getAgentCount() < license.getSupportedAgents())
        {
            return AUTH;
        }

        return new String[0];
    }

    /**
     * Required resource.
     *
     * @param agentManager
     */
    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }


}
