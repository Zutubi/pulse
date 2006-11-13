package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.logging.LogConfiguration;
import com.zutubi.pulse.logging.LogConfigurationManager;
import com.zutubi.pulse.web.ActionSupport;

import java.util.List;

/**
 * <class-comment/>
 */
public class LogConfigurationAction extends ActionSupport
{
    private String config;

    private boolean eventLoggingEnabled;

    private List<String> configs;

    private LogConfigurationManager logConfigurationManager;

    private MasterConfigurationManager configurationManager;

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
        logConfigurationManager.setEventLoggingEnabled(eventLoggingEnabled);

        return SUCCESS;
    }

    public String execute()
    {
        loadConfig();
        return SUCCESS;
    }

    private void saveConfig()
    {
        LogConfiguration logConfig = configurationManager.getAppConfig();
        logConfig.setLoggingLevel(config);
        logConfig.setEventLoggingEnabled(eventLoggingEnabled);
    }

    private void loadConfig()
    {
        configs = logConfigurationManager.getAvailableConfigurations();

        LogConfiguration logConfig = configurationManager.getAppConfig();
        config = logConfig.getLoggingLevel();
        eventLoggingEnabled = logConfig.isEventLoggingEnabled();
    }

    private void resetConfig()
    {
        LogConfiguration logConfig = configurationManager.getAppConfig();
        logConfig.setEventLoggingEnabled(false);
        logConfig.setLoggingLevel(null);
    }

    public List<String> getConfigs()
    {
        return configs;
    }

    public boolean isEventLoggingEnabled()
    {
        return eventLoggingEnabled;
    }

    public void setEventLoggingEnabled(boolean eventLoggingEnabled)
    {
        this.eventLoggingEnabled = eventLoggingEnabled;
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

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

}
