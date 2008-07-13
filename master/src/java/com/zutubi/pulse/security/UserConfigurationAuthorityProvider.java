package com.zutubi.pulse.security;

import com.zutubi.pulse.tove.config.user.UserConfiguration;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

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
        accessManager.registerAuthorityProvider(UserConfiguration.class, this);
    }
}
