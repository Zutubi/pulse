package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.GuestAccessManager;
import com.zutubi.pulse.bootstrap.ApplicationConfiguration;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 * <class-comment/>
 */
public class GeneralConfigurationAction extends ActionSupport
{
    private ConfigurationManager configurationManager;
    private GuestAccessManager guestAccessManager;

    private String hostName;
    private String helpUrl;
    private boolean rssEnabled;
    private boolean anonEnabled;

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

    public boolean isRssEnabled()
    {
        return rssEnabled;
    }

    public void setRssEnabled(boolean rssEnabled)
    {
        this.rssEnabled = rssEnabled;
    }

    public boolean isAnonEnabled()
    {
        return anonEnabled;
    }

    public void setAnonEnabled(boolean anonEnabled)
    {
        this.anonEnabled = anonEnabled;
    }

    private void resetConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setHostName(null);
        config.setHelpUrl(null);
        config.setRssEnabled(null);
        config.setAnonymousAccessEnabled(null);
        guestAccessManager.init();
    }

    private void saveConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setHostName(hostName);
        config.setHelpUrl(helpUrl);
        config.setRssEnabled(rssEnabled);
        config.setAnonymousAccessEnabled(anonEnabled);
        guestAccessManager.init();
    }

    private void loadConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        hostName = config.getHostName();
        helpUrl = config.getHelpUrl();
        rssEnabled = config.getRssEnabled();
        anonEnabled = config.getAnonymousAccessEnabled();
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setGuestAccessManager(GuestAccessManager guestAccessManager)
    {
        this.guestAccessManager = guestAccessManager;
    }
}
