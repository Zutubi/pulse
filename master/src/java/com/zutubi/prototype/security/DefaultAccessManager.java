package com.zutubi.prototype.security;

import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class DefaultAccessManager implements AccessManager
{
    private static final Logger LOG = Logger.getLogger(DefaultAccessManager.class);

    private AuthorityProvider globalProvider;
    private Map<Class, AuthorityProvider> providers = new HashMap<Class, AuthorityProvider>();
    private Set<String> defaultAuthorities = new HashSet<String>();
    private Set<String> superAuthorities = new HashSet<String>();

    public void registerProvider(AuthorityProvider<Object> provider)
    {
        globalProvider = provider;
    }

    public <T> void registerProvider(Class<T> clazz, AuthorityProvider<T> provider)
    {
        providers.put(clazz, provider);
    }

    public void addSuperAuthority(String authority)
    {
        superAuthorities.add(authority);
    }

    public boolean hasPermission(Actor actor, String action, Object resource)
    {
        Set<String> granted = actor == null ? defaultAuthorities : actor.getGrantedAuthorities();

        // If the actor has super privileges, then there is no need to check
        // any further: they're in.
        for(String authority: granted)
        {
            if(superAuthorities.contains(authority))
            {
                return true;
            }
        }

        AuthorityProvider provider = getProvider(resource);
        if(provider == null)
        {
            // Be conservative: if the resource is deliberately unprotected,
            // then this question should not be asked.  Hence we assume a
            // mistake and refuse access just in case.            
            LOG.warning("Request to access resource of type '" + resource.getClass().getName() + "' denied as no provider was found");
            return false;
        }

        Set<String> allowed = provider.getAllowedAuthorities(action, resource);
        for(String authority: allowed)
        {
            if(granted.contains(authority))
            {
                return true;
            }
        }

        return false;
    }

    private AuthorityProvider getProvider(Object resource)
    {
        AuthorityProvider provider;
        if (resource == null)
        {
            provider = globalProvider;
        }
        else
        {
            Class clazz = resource.getClass();
            provider = providers.get(clazz);
        }
        return provider;
    }
}
