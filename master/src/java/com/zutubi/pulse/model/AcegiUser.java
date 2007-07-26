package com.zutubi.pulse.model;

import org.acegisecurity.userdetails.UserDetails;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class AcegiUser implements UserDetails
{
    private String username;
    private String password;
    private GrantedAuthority[] authorities;
    /**
     * Authorities granted for this session to this user.  These include
     * authorities authorities inherited by being part of an LDAP group.
     */
    private List<String> transientAuthorities = new LinkedList<String>();
    private boolean enabled;
    private boolean ldapAuthentication;

    public AcegiUser(User user)
    {
        username = user.getLogin();
        password = user.getPassword();
        initAuthorities(user);
        enabled = user.isEnabled();
        ldapAuthentication = user.getLdapAuthentication();
    }

    private void initAuthorities(User user)
    {
        List<String> directAuthorities = user.getGrantedAuthorities();
        int total = directAuthorities.size();

        for(Group g: user.getGroups())
        {
            total += g.getAuthorityCount();
        }

        authorities = new GrantedAuthority[total];
        int i = 0;
        for(String authority: directAuthorities)
        {
            authorities[i++] = new GrantedAuthority(authority);
        }

        for(String authority: transientAuthorities)
        {
            authorities[i++] = new GrantedAuthority(authority);
        }

        for(Group g: user.getGroups())
        {
            for(GrantedAuthority authority: g.getAuthorities())
            {
                authorities[i++] = authority;
            }
        }
    }

    public org.acegisecurity.GrantedAuthority[] getAuthorities()
    {
        int total = authorities.length + transientAuthorities.size();
        GrantedAuthority[] result = new GrantedAuthority[total];

        System.arraycopy(authorities, 0, result, 0, authorities.length);

        int i = authorities.length;
        for(String authority: transientAuthorities)
        {
            result[i++] = new GrantedAuthority(authority);
        }

        return result;
    }

    public boolean hasAuthority(String authority)
    {
        org.acegisecurity.GrantedAuthority[] authorities = getAuthorities();
        for(org.acegisecurity.GrantedAuthority a: authorities)
        {
            if(a.getAuthority().equals(authority))
            {
                return true;
            }
        }

        return false;
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

    public void addTransientAuthority(String authority)
    {
        if(!transientAuthorities.contains(authority))
        {
            transientAuthorities.add(authority);
        }
    }
}
