package com.zutubi.tove.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic implementation of the Actor interface.
 */
public class DefaultActor implements Actor
{
    private String username;
    private Set<String> grantedAuthorities = new HashSet<String>();

    public DefaultActor(String username, String... authorities)
    {
        this.username = username;
        grantedAuthorities.addAll(Arrays.asList(authorities));
    }

    public String getUsername()
    {
        return username;
    }

    public Set<String> getGrantedAuthorities()
    {
        return grantedAuthorities;
    }

    public boolean isAnonymous()
    {
        return false;
    }

    public void addGrantedAuthority(String authority)
    {
        grantedAuthorities.add(authority);
    }
}
