package com.zutubi.pulse.web.user;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.web.ActionSupport;

/**
 * An action to clear an error from a contact point.
 */
public class ClearContactPointErrorAction extends ActionSupport
{
    // FIXME: the ui will not pass this!
    private String path;
    private ConfigurationProvider configurationProvider;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute()
    {
        ContactConfiguration contact = configurationProvider.get(path, ContactConfiguration.class);
        if (contact != null)
        {
            contact.clearLastError();
            configurationProvider.save(contact);
        }
        
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
