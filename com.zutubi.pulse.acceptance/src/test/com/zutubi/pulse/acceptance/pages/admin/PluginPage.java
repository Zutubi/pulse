package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The plugin view page.
 */
public class PluginPage extends SeleniumPage
{
    private static final String ID_PLUGIN_ERROR = "plugin.error";
    
    private String pluginId;

    public PluginPage(SeleniumBrowser browser, Urls urls, String pluginId)
    {
        super(browser, urls, "details:" + pluginId);
        this.pluginId = pluginId;
    }

    public String getUrl()
    {
        return urls.adminPlugin(pluginId);
    }

    public String getState()
    {
        return browser.getText("plugin.status");
    }

    public boolean isErrorMessagePresent()
    {
        return browser.isElementIdPresent(ID_PLUGIN_ERROR);
    }

    public String getErrorMessage()
    {
        return browser.getText(ID_PLUGIN_ERROR);
    }
}
