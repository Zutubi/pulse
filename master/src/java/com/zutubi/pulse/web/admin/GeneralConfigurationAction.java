/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.ApplicationConfiguration;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 * <class-comment/>
 */
public class GeneralConfigurationAction extends ActionSupport
{
    private ConfigurationManager configurationManager;

    private String hostName;
    private String helpUrl;

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

    public String getHelpUrl()
    {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl)
    {
        this.helpUrl = helpUrl;
    }

    private void resetConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setHostName(null);
        config.setHelpUrl(null);
    }

    private void saveConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setHostName(hostName);
        config.setHelpUrl(helpUrl);
    }

    private void loadConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        hostName = config.getHostName();
        helpUrl = config.getHelpUrl();
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
