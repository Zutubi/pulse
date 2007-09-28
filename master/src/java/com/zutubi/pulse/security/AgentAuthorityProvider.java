package com.zutubi.pulse.security;

import com.zutubi.prototype.security.AuthorityProvider;
import com.zutubi.prototype.security.DefaultAccessManager;
import com.zutubi.pulse.agent.Agent;

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
