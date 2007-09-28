package com.zutubi.pulse.security;

import com.zutubi.prototype.security.Actor;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;

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
}
