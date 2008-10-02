package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.master.agent.AgentManager;

/**
 * <class-comment/>
 */
public class AddAgentAuthorisation implements Authorisation
{
    private AgentManager agentManager;

    public static final String[] AUTH = {LicenseHolder.AUTH_ADD_AGENT};

    public String[] getAuthorisation(License license)
    {
        if (license == null)
        {
            return NO_AUTH;
        }
        
        if (license.getSupportedAgents() == License.UNRESTRICTED)
        {
            return AUTH;
        }

        if (agentManager.getAgentCount() < license.getSupportedAgents())
        {
            return AUTH;
        }

        return NO_AUTH;
    }

    /**
     * Required resource.
     *
     * @param agentManager instance
     */
    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }


}
