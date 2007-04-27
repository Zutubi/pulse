package com.zutubi.pulse;

import com.zutubi.prototype.config.ConfigurationEventListener;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.anonymous.AnonymousProcessingFilter;
import org.acegisecurity.userdetails.memory.UserAttribute;

/**
 */
public class GuestAccessManager implements ConfigurationEventListener
{
    private AnonymousProcessingFilter anonymousProcessingFilter;
    private ConfigurationProvider configurationProvider;

    public void init()
    {
        UserAttribute userAttribute = anonymousProcessingFilter.getUserAttribute();
        UserAttribute newAttribute = new UserAttribute();

        newAttribute.setPassword(userAttribute.getPassword());
        newAttribute.addAuthority(new GrantedAuthorityImpl(GrantedAuthority.ANONYMOUS));
        if(configurationProvider.get(GeneralAdminConfiguration.class).isAnonymousAccessEnabled())
        {
            newAttribute.addAuthority(new GrantedAuthorityImpl(GrantedAuthority.GUEST));
        }

        anonymousProcessingFilter.setUserAttribute(newAttribute);
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if(event instanceof PostSaveEvent)
        {
            init();
        }
    }

    public void setAnonymousProcessingFilter(AnonymousProcessingFilter anonymousProcessingFilter)
    {
        this.anonymousProcessingFilter = anonymousProcessingFilter;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
        configurationProvider.registerEventListener(this, false, false, GeneralAdminConfiguration.class);
    }
}
