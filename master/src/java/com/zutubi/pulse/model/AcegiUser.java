package com.zutubi.pulse.model;

import org.acegisecurity.userdetails.UserDetails;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class AcegiUser implements UserDetails
{
    private User user;
    /**
     * Authorities granted for this session to this user.  These include
     * authorities authorities inherited by being part of an LDAP group.
     */
    private List<String> transientAuthorities = new LinkedList<String>();


    public AcegiUser(User user)
    {
        this.user = user;
    }

    public org.acegisecurity.GrantedAuthority[] getAuthorities()
    {
        List<String> directAuthorities = user.getGrantedAuthorities();
        int total = directAuthorities.size() + transientAuthorities.size();

        for(Group g: user.getGroups())
        {
            total += g.getAuthorityCount();
        }

        GrantedAuthority[] result = new GrantedAuthority[total];
        int i = 0;
        for(String authority: directAuthorities)
        {
            result[i++] = new GrantedAuthority(authority);
        }

        for(String authority: transientAuthorities)
        {
            result[i++] = new GrantedAuthority(authority);
        }

        for(Group g: user.getGroups())
        {
            for(GrantedAuthority authority: g.getAuthorities())
            {
                result[i++] = authority;
            }
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
        return user.getPassword();
    }

    public String getUsername()
    {
        return user.getLogin();
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
        return user.isEnabled();
    }

    public void addTransientAuthority(String authority)
    {
        if(!transientAuthorities.contains(authority))
        {
            transientAuthorities.add(authority);
        }
    }

    public User getUser()
    {
        return user;
    }
}
