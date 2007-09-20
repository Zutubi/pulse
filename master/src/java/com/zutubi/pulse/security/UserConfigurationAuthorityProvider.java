package com.zutubi.pulse.security;

import com.zutubi.prototype.security.AuthorityProvider;
import com.zutubi.prototype.security.DefaultAccessManager;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows users to perform actions on their own configuration.
 */
public class UserConfigurationAuthorityProvider implements AuthorityProvider<UserConfiguration>
{
    public Set<String> getAllowedAuthorities(String action, UserConfiguration resource)
    {
        Set<String> result = new HashSet<String>(1);
        // The user can do whatever to themself.
        result.add(resource.getDefaultAuthority());
        return result;
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerProvider(UserConfiguration.class, this);
    }
}
