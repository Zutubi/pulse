package com.cinnamonbob.web.admin;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.ApplicationConfiguration;

/**
 * <class-comment/>
 */
public class GeneralConfigurationAction extends ActionSupport
{
    private ConfigurationManager configurationManager;

    private String hostName;

    public String doReset()
    {
        resetConfig();
        loadConfig();
        return SUCCESS;
    }

    public String doSave()
    {
        saveConfig();

        return SUCCESS;
    }

    public String doInput()
    {
        loadConfig();

        return INPUT;
    }

    public String execute()
    {
        // default action, load the config details.
        loadConfig();

        return SUCCESS;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    private void resetConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setHostName(null);
    }

    private void saveConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setHostName(hostName);
    }

    private void loadConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        hostName = config.getHostName();
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
