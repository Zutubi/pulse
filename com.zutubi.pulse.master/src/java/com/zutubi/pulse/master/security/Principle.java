package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.security.Actor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 */
public class Principle implements Actor, UserDetails
{
    private long id;
    private String username;
    private String password;
    private Set<String> authoritySet;
    private List<GrantedAuthority> authorities = null;
    private boolean enabled;
    private boolean ldapAuthentication;

    public Principle(long id, String username, String pass)
    {
        this.id = id;
        this.username = username;
        this.password = pass;
        this.enabled = true;
        this.authorities = new LinkedList<GrantedAuthority>();
        this.ldapAuthentication = true;
    }

    public Principle(User user, List<UserGroupConfiguration> groups)
    {
        this(user.getConfig(), groups);
        enabled = user.isEnabled();
    }

    public Principle(UserConfiguration config, List<UserGroupConfiguration> groups)
    {
        id = config.getUserId();
        username = config.getLogin();
        password = config.getPassword();
        initAuthorities(config, groups);
        enabled = true;
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

    private synchronized void initAuthorities(UserConfiguration config, List<UserGroupConfiguration> groups)
    {
        authoritySet = new HashSet<String>();
        authoritySet.addAll(Arrays.asList(config.getGrantedAuthorities()));

        if (groups != null)
        {
            for(UserGroupConfiguration g: groups)
            {
                addGroupAuthorities(g);
            }
        }
    }

    private void addGroupAuthorities(GroupConfiguration g)
    {
        authoritySet.addAll(Arrays.asList(g.getGrantedAuthorities()));
    }

    public synchronized Collection<GrantedAuthority> getAuthorities()
    {
        if(authorities == null)
        {
            authorities = new LinkedList<GrantedAuthority>();
            for(String authority: authoritySet)
            {
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        return authorities;
    }

    public long getId()
    {
        return id;
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

    public synchronized void addGroup(GroupConfiguration group)
    {
        addGroupAuthorities(group);
        authorities = null;
    }
}
