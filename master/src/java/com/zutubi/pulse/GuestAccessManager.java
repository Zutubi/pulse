package com.zutubi.pulse;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.system.ConfigurationEventSystemStartedEvent;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.tove.config.group.AbstractGroupConfiguration;
import com.zutubi.pulse.tove.config.group.BuiltinGroupConfiguration;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.config.events.PostSaveEvent;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.anonymous.AnonymousProcessingFilter;
import org.acegisecurity.userdetails.memory.UserAttribute;

/**
 */
public class GuestAccessManager implements ConfigurationEventListener, EventListener
{
    private static final Logger LOG = Logger.getLogger(GuestAccessManager.class);

    private AnonymousProcessingFilter anonymousProcessingFilter;
    private ConfigurationProvider configurationProvider;

    public synchronized void init()
    {
        UserAttribute userAttribute = anonymousProcessingFilter.getUserAttribute();
        UserAttribute newAttribute = new UserAttribute();

        newAttribute.setPassword(userAttribute.getPassword());
        newAttribute.addAuthority(new GrantedAuthorityImpl(GrantedAuthority.ANONYMOUS));
        if(configurationProvider.get(GlobalConfiguration.class).isAnonymousAccessEnabled())
        {
            BuiltinGroupConfiguration group = configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, UserManager.ANONYMOUS_USERS_GROUP_NAME), BuiltinGroupConfiguration.class);
            if(group != null)
            {
                for(String authority: group.getGrantedAuthorities())
                {
                    newAttribute.addAuthority(new GrantedAuthorityImpl(authority));
                }
            }
        }

        anonymousProcessingFilter.setUserAttribute(newAttribute);
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if(event instanceof PostSaveEvent)
        {
            LOG.fine("Refreshing anonymous group authorities");
            init();
        }
    }

    public void handleEvent(Event event)
    {
        configurationProvider = ((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider();
        configurationProvider.registerEventListener(this, true, false, GlobalConfiguration.class);
        configurationProvider.registerEventListener(this, true, true, AbstractGroupConfiguration.class);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class };
    }

    public void setAnonymousProcessingFilter(AnonymousProcessingFilter anonymousProcessingFilter)
    {
        this.anonymousProcessingFilter = anonymousProcessingFilter;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
