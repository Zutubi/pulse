package com.zutubi.pulse.master.security;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.model.Role;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides allowed authorities for global (i.e. server-wide) actions.
 */
public class GlobalAuthorityProvider implements AuthorityProvider<Object>, EventListener
{
    public static final String CREATE_USER = "CREATE_USER";

    private ConfigurationProvider configurationProvider;

    public Set<String> getAllowedAuthorities(String action, Object resource)
    {
        Set<String> result = new HashSet<String>();
        result.add(action);

        if(CREATE_USER.equals(action))
        {
            GlobalConfiguration config = configurationProvider.get(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
            if(config.isAnonymousSignupEnabled())
            {
                result.add(Role.ANONYMOUS);
                result.add(Role.GUEST);
                result.add(Role.USER);
            }
        }

        return result;
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.addSuperAuthority(ServerPermission.ADMINISTER.toString());
        accessManager.registerAuthorityProvider(this);
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void handleEvent(Event event)
    {
        this.configurationProvider = ((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class };
    }
}
