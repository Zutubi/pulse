package com.zutubi.pulse.master.security;

import com.zutubi.tove.security.Actor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple actor that has no username but may be granted authorities.
 */
public class AnonymousActor implements Actor
{
    private Set<String> grantedAuthorities = new HashSet<String>();

    public AnonymousActor(Authentication authentication)
    {
        for(GrantedAuthority a: authentication.getAuthorities())
        {
            grantedAuthorities.add(a.getAuthority());
        }
    }

    public String getUsername()
    {
        return null;
    }

    public Set<String> getGrantedAuthorities()
    {
        return grantedAuthorities;
    }

    public boolean isAnonymous()
    {
        return true;
    }
}
