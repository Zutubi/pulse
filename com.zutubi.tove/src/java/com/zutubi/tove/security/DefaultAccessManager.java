package com.zutubi.tove.security;

import com.zutubi.util.logging.Logger;
import org.springframework.security.AccessDeniedException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class DefaultAccessManager implements AccessManager
{
    private static final Logger LOG = Logger.getLogger(DefaultAccessManager.class);

    private ActorProvider actorProvider;
    private AuthorityProvider globalAuthorityProvider;
    private Map<Class, AuthorityProvider> authorityProviders = new HashMap<Class, AuthorityProvider>();
    private Set<String> defaultAuthorities = new HashSet<String>();
    private Set<String> superAuthorities = new HashSet<String>();

    public void registerAuthorityProvider(AuthorityProvider<Object> provider)
    {
        globalAuthorityProvider = provider;
    }

    public <T> void registerAuthorityProvider(Class<T> clazz, AuthorityProvider<T> provider)
    {
        authorityProviders.put(clazz, provider);
    }

    public void addSuperAuthority(String authority)
    {
        superAuthorities.add(authority);
    }

    public Actor getActor()
    {
        return actorProvider.getActor();
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
            String type = resource == null ? "<null>" : resource.getClass().getName();
            LOG.warning("Request to access resource of type '" + type + "' denied as no provider was found");
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

    public boolean hasPermission(String action, Object resource)
    {
        return hasPermission(getActor(), action, resource);
    }

    public void ensurePermission(Actor actor, String action, Object resource)
    {
        if(!hasPermission(actor, action, resource))
        {
            throw new AccessDeniedException("Permission to perform action '" + action + "' denied");
        }
    }

    public void ensurePermission(String action, Object resource)
    {
        ensurePermission(getActor(), action, resource);
    }

    private AuthorityProvider getProvider(Object resource)
    {
        AuthorityProvider provider;
        if (resource == null)
        {
            provider = globalAuthorityProvider;
        }
        else
        {
            // Try direct hit on class
            provider = authorityProviders.get(resource.getClass());
            if(provider == null)
            {
                // OK, see if a superclass is known.
                for(Map.Entry<Class, AuthorityProvider> entry: authorityProviders.entrySet())
                {
                    if(entry.getKey().isInstance(resource))
                    {
                        provider = entry.getValue();
                        break;
                    }
                }
            }
        }
        return provider;
    }

    public void setActorProvider(ActorProvider actorProvider)
    {
        this.actorProvider = actorProvider;
    }
}
