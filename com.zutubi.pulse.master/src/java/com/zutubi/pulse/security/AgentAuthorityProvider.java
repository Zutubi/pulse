package com.zutubi.pulse.security;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.Set;

/**
 * Maps from agent ACL configurations to authorities allowed to perform
 * actions on agents.
 */
public class AgentAuthorityProvider implements AuthorityProvider<Agent>
{
    private AgentConfigurationAuthorityProvider agentConfigurationAuthorityProvider;

    public Set<String> getAllowedAuthorities(String action, Agent resource)
    {
        return agentConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getConfig());
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(Agent.class, this);
    }

    public void setAgentConfigurationAuthorityProvider(AgentConfigurationAuthorityProvider agentConfigurationAuthorityProvider)
    {
        this.agentConfigurationAuthorityProvider = agentConfigurationAuthorityProvider;
    }
}
