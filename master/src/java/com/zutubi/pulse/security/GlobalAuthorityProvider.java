package com.zutubi.pulse.security;

import com.zutubi.prototype.security.AuthorityProvider;
import com.zutubi.prototype.security.DefaultAccessManager;
import com.zutubi.pulse.prototype.config.group.ServerPermission;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides allowed authorities for global (i.e. server-wide) actions.
 */
public class GlobalAuthorityProvider implements AuthorityProvider<Object>
{
    public Set<String> getAllowedAuthorities(String action, Object resource)
    {
        Set<String> result = new HashSet<String>(1);
        result.add(action);
        return result;
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.addSuperAuthority(ServerPermission.ADMINISTER.toString());
        accessManager.registerProvider(this);
    }
}
