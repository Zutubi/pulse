package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.GuestAccessManager;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 * <class-comment/>
 */
public class GeneralConfigurationAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;
    private GuestAccessManager guestAccessManager;

    private String baseUrl;
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

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
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
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setBaseUrl(null);
        config.setHelpUrl(null);
        config.setRssEnabled(null);
        config.setAnonymousAccessEnabled(null);
        guestAccessManager.init();
    }

    private void saveConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setBaseUrl(baseUrl);
        config.setHelpUrl(helpUrl);
        config.setRssEnabled(rssEnabled);
        config.setAnonymousAccessEnabled(anonEnabled);
        guestAccessManager.init();
    }

    private void loadConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        baseUrl = config.getBaseUrl();
        helpUrl = config.getHelpUrl();
        rssEnabled = config.getRssEnabled();
        anonEnabled = config.getAnonymousAccessEnabled();
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setGuestAccessManager(GuestAccessManager guestAccessManager)
    {
        this.guestAccessManager = guestAccessManager;
    }
}
