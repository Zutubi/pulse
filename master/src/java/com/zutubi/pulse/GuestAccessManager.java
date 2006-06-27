package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.model.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.anonymous.AnonymousProcessingFilter;
import org.acegisecurity.userdetails.memory.UserAttribute;

/**
 */
public class GuestAccessManager
{
    private AnonymousProcessingFilter anonymousProcessingFilter;
    private ConfigurationManager configurationManager;

    public void init()
    {
        UserAttribute userAttribute = anonymousProcessingFilter.getUserAttribute();
        UserAttribute newAttribute = new UserAttribute();

        newAttribute.setPassword(userAttribute.getPassword());
        newAttribute.addAuthority(new GrantedAuthorityImpl(GrantedAuthority.ANONYMOUS));
        if(configurationManager.getAppConfig().getAnonymousAccessEnabled())
        {
            newAttribute.addAuthority(new GrantedAuthorityImpl(GrantedAuthority.GUEST));
        }

        anonymousProcessingFilter.setUserAttribute(newAttribute);
    }


    public void setAnonymousProcessingFilter(AnonymousProcessingFilter anonymousProcessingFilter)
    {
        this.anonymousProcessingFilter = anonymousProcessingFilter;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
