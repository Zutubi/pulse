package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.GrantedAuthority;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.group.AbstractGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.security.Actor;
import org.acegisecurity.userdetails.UserDetails;

import java.util.Arrays;
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

    public AcegiUser(String user, String pass)
    {
        this.username = user;
        this.password = pass;
        this.enabled = true;
        this.authorities = new GrantedAuthority[0];
        this.ldapAuthentication = true;
    }

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

    public boolean isAnonymous()
    {
        return false;
    }

    private synchronized void initAuthorities(UserConfiguration config, List<GroupConfiguration> groups)
    {
        authoritySet = new HashSet<String>();
        authoritySet.addAll(Arrays.asList(config.getGrantedAuthorities()));

        if (groups != null)
        {
            for(GroupConfiguration g: groups)
            {
                addGroupAuthorities(g);
            }
        }
    }

    private void addGroupAuthorities(AbstractGroupConfiguration g)
    {
        authoritySet.addAll(Arrays.asList(g.getGrantedAuthorities()));
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

    public synchronized void addGroup(AbstractGroupConfiguration group)
    {
        addGroupAuthorities(group);
        authorities = null;
    }
}
