package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.model.Role;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.group.BuiltinGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.config.events.PostSaveEvent;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.logging.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.List;

import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;

/**
 */
public class GuestAccessManager implements ConfigurationEventListener, EventListener
{
    private static final Logger LOG = Logger.getLogger(GuestAccessManager.class);

    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;
    private ConfigurationProvider configurationProvider;

    public synchronized void init()
    {
        // Not entirely sure if its kosher to modify this list in place, but it appears to work.
        List<GrantedAuthority> authorities = anonymousAuthenticationFilter.getAuthorities();
        authorities.clear();
        authorities.add(new SimpleGrantedAuthority(Role.ANONYMOUS));
        if (configurationProvider.get(GlobalConfiguration.class).isAnonymousAccessEnabled())
        {
            BuiltinGroupConfiguration group = configurationProvider.get(PathUtils.getPath(GROUPS_SCOPE, ANONYMOUS_USERS_GROUP_NAME), BuiltinGroupConfiguration.class);
            if (group != null)
            {
                for (String authority: group.getGrantedAuthorities())
                {
                    authorities.add(new SimpleGrantedAuthority(authority));
                }
            }
        }
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if (event instanceof PostSaveEvent)
        {
            LOG.fine("Refreshing anonymous group authorities");
            init();
        }
    }

    public void handleEvent(Event event)
    {
        if (event instanceof ConfigurationEventSystemStartedEvent)
        {
            configurationProvider = ((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider();
            configurationProvider.registerEventListener(this, true, false, GlobalConfiguration.class);
            configurationProvider.registerEventListener(this, true, true, GroupConfiguration.class);
        }
        else
        {
            init();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class, ConfigurationSystemStartedEvent.class };
    }

    public void setAnonymousAuthenticationFilter(AnonymousAuthenticationFilter anonymousAuthenticationFilter)
    {
        this.anonymousAuthenticationFilter = anonymousAuthenticationFilter;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
