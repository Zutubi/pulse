package com.zutubi.pulse.model;

import com.zutubi.prototype.security.Actor;
import com.zutubi.pulse.prototype.config.group.GroupConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import org.acegisecurity.userdetails.UserDetails;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class AcegiUser implements Actor, UserDetails
{
    private String username;
    private String password;
    private Set<String> authoritySet;
    private GrantedAuthority[] authorities = null;
    private boolean enabled;
    private boolean ldapAuthentication;

    public AcegiUser(User user, List<GroupConfiguration> groups)
    {
        UserConfiguration config = user.getConfig();
        username = config.getLogin();
        password = config.getPassword();
        initAuthorities(config, groups);
        enabled = user.isEnabled();
        ldapAuthentication = config.isAuthenticatedViaLdap();
    }

    public Set<String> getGrantedAuthorities()
    {
        return authoritySet;
    }

    private synchronized void initAuthorities(UserConfiguration config, List<GroupConfiguration> groups)
    {
        authoritySet = new HashSet<String>();
        for(String a: config.getGrantedAuthorities())
        {
            authoritySet.add(a);
        }

        if (groups != null)
        {
            for(GroupConfiguration g: groups)
            {
                for(String a: g.getGrantedAuthorities())
                {
                    authoritySet.add(a);
                }
            }
        }
    }

    public synchronized org.acegisecurity.GrantedAuthority[] getAuthorities()
    {
        if(authorities == null)
        {
            authorities = new GrantedAuthority[authoritySet.size()];
            int i = 0;
            for(String authority: authoritySet)
            {
                authorities[i++] = new GrantedAuthority(authority);
            }
        }

        return authorities;
    }

    public synchronized boolean hasAuthority(String authority)
    {
        return authoritySet.contains(authority);
    }

    public String getPassword()
    {
        return password;
    }

    public String getUsername()
    {
        return username;
    }

    public boolean isAccountNonExpired()
    {
        return true;
    }

    public boolean isAccountNonLocked()
    {
        return true;
    }

    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public boolean getLdapAuthentication()
    {
        return ldapAuthentication;
    }

    public synchronized void addTransientAuthority(String authority)
    {
        authoritySet.add(authority);
        authorities = null;
    }
}
