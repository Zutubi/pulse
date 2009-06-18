package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.GrantedAuthority;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows users to perform actions on their own configuration.
 */
public class GroupConfigurationAuthorityProvider implements AuthorityProvider<GroupConfiguration>
{
    public Set<String> getAllowedAuthorities(String action, GroupConfiguration resource)
    {
        Set<String> result = new HashSet<String>(1);
        if (AccessManager.ACTION_VIEW.equals(action))
        {
            result.add(GrantedAuthority.USER);
        }
        return result;
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(GroupConfiguration.class, this);
    }
}