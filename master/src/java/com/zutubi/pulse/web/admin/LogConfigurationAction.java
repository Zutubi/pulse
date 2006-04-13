/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.logging.LogConfigurationManager;
import com.zutubi.pulse.bootstrap.ConfigurationManager;

import java.util.List;

/**
 * <class-comment/>
 */
public class LogConfigurationAction extends ActionSupport
{
    private String config;

    private List<String> configs;

    private LogConfigurationManager logConfigurationManager;

    private ConfigurationManager configurationManager;

    public String doReset()
    {
        resetConfig();
        loadConfig();
        return SUCCESS;
    }

    public String doInput()
    {
        loadConfig();
        return "input";
    }

    public String doSave()
    {
        saveConfig();

        // update the configuration change.
        logConfigurationManager.updateConfiguration(config);

        return SUCCESS;
    }

    public String execute()
    {
        loadConfig();
        return SUCCESS;
    }

    private void saveConfig()
    {
        configurationManager.getAppConfig().setLogConfig(config);
    }

    private void loadConfig()
    {
        configs = logConfigurationManager.getAvailableConfigurations();
        config = configurationManager.getAppConfig().getLogConfig();
    }

    private void resetConfig()
    {
        configurationManager.getAppConfig().setLogConfig(null);
    }

    public List<String> getConfigs()
    {
        return configs;
    }

    public String getConfig()
    {
        return config;
    }

    public void setConfig(String config)
    {
        this.config = config;
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

}
