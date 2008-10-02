package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The plugin view page.
 */
public class PluginPage extends SeleniumPage
{
    private static final String ID_PLUGIN_ERROR = "plugin.error";
    
    private String pluginId;

    public PluginPage(Selenium selenium, Urls urls, String pluginId)
    {
        super(selenium, urls, "details:" + pluginId);
        this.pluginId = pluginId;
    }

    public String getUrl()
    {
        return urls.adminPlugin(pluginId);
    }

    public String getState()
    {
        return selenium.getText("plugin.status");
    }

    public boolean isErrorMessagePresent()
    {
        return selenium.isElementPresent(ID_PLUGIN_ERROR);
    }

    public String getErrorMessage()
    {
        return selenium.getText(ID_PLUGIN_ERROR);
    }
}
