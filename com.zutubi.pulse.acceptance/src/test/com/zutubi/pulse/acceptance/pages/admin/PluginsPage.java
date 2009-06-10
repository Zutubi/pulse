package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.InstallPluginForm;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The all plugins list.
 */
public class PluginsPage extends SeleniumPage
{
    public PluginsPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "all.plugins", "plugins");
    }

    public String getUrl()
    {
        return urls.adminPlugins();
    }

    public boolean isPluginPresent(String id)
    {
        return browser.isElementIdPresent("plugin:" + id);
    }

    public boolean isActionPresent(String id, String action)
    {
        return browser.isElementIdPresent(getActionId(action, id));
    }
    
    public String getPluginState(String id)
    {
        return browser.getText("status:" + id);
    }

    public PluginPage clickPlugin(String id)
    {
        browser.click("select:" + id);
        return browser.createPage(PluginPage.class, id);
    }

    public InstallPluginForm clickInstall()
    {
        browser.click("plugin.add");
        return new InstallPluginForm(browser);
    }

    public void clickDisable(String id)
    {
        browser.click(getActionId("disable", id));
    }

    public void clickEnable(String id)
    {
        browser.click(getActionId("enable", id));
    }

    public void clickUninstall(String id)
    {
        browser.click(getActionId("uninstall", id));
    }

    public String getActionId(String action, String id)
    {
        return action + ":" + id;
    }
}
