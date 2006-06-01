package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.ApplicationConfiguration;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class ServerSettingsAction extends ActionSupport
{
    ConfigurationManager configurationManager;
    JabberManager jabberManager;
    ApplicationConfiguration config;

    public ApplicationConfiguration getConfig()
    {
        return config;
    }

    public String getJabberStatus()
    {
        return jabberManager.getStatusMessage();
    }

    public String execute() throws Exception
    {
        config = configurationManager.getAppConfig();
        return SUCCESS;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
