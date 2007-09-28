package com.zutubi.pulse.security;

import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.security.AuthorityProvider;
import com.zutubi.prototype.security.DefaultAccessManager;
import com.zutubi.pulse.prototype.config.agent.AgentAclConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Maps from agent ACL configurations to authorities allowed to perform
 * actions on agents.
 */
public class AgentConfigurationAuthorityProvider implements AuthorityProvider<AgentConfiguration>
{
    public Set<String> getAllowedAuthorities(String action, AgentConfiguration resource)
    {
        Set<String> result = new HashSet<String>();

        // See what groups have been granted the authority to perform this
        // action (or can administer the project).
        for(AgentAclConfiguration acl: resource.getPermissions())
        {
            List<String> allowedActions = acl.getAllowedActions();
            if(allowedActions.contains(AccessManager.ACTION_ADMINISTER) || allowedActions.contains(action))
            {
                result.add(acl.getGroup().getDefaultAuthority());
            }
        }

        return result;
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(AgentConfiguration.class, this);
    }
}
