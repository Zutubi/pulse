package com.cinnamonbob.web.admin;

import com.cinnamonbob.bootstrap.ApplicationConfiguration;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.web.ActionSupport;

/**
 */
public class ServerSettingsAction extends ActionSupport
{
    ConfigurationManager configurationManager;
    ApplicationConfiguration config;

    public ApplicationConfiguration getConfig()
    {
        return config;
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
}
