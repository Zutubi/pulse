package com.zutubi.pulse.security;

import com.zutubi.prototype.security.AuthorityProvider;
import com.zutubi.prototype.security.DefaultAccessManager;
import com.zutubi.pulse.model.User;

import java.util.Set;

/**
 * Provides authorities for users by delegating to
 * {@link com.zutubi.pulse.security.UserConfigurationAuthorityProvider}.
 */
public class UserAuthorityProvider implements AuthorityProvider<User>
{
    private UserConfigurationAuthorityProvider userConfigurationAuthorityProvider;

    public Set<String> getAllowedAuthorities(String action, User resource)
    {
        return userConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getConfig());
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(User.class, this);
    }

    public void setUserConfigurationAuthorityProvider(UserConfigurationAuthorityProvider userConfigurationAuthorityProvider)
    {
        this.userConfigurationAuthorityProvider = userConfigurationAuthorityProvider;
    }
}
