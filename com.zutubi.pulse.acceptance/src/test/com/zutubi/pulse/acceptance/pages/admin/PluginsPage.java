package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.InstallPluginForm;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The all plugins list.
 */
public class PluginsPage extends SeleniumPage
{
    public PluginsPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "all.plugins", "plugins");
    }

    public String getUrl()
    {
        return urls.adminPlugins();
    }

    public boolean isPluginPresent(String id)
    {
        return selenium.isElementPresent("plugin:" + id);
    }

    public boolean isActionPresent(String id, String action)
    {
        return selenium.isElementPresent(getActionId(action, id));
    }
    
    public String getPluginState(String id)
    {
        return selenium.getText("status:" + id);
    }

    public PluginPage clickPlugin(String id)
    {
        selenium.click("select:" + id);
        return new PluginPage(selenium, urls, id);
    }

    public InstallPluginForm clickInstall()
    {
        selenium.click("plugin.add");
        return new InstallPluginForm(selenium);
    }

    public void clickDisable(String id)
    {
        selenium.click(getActionId("disable", id));
    }

    public void clickEnable(String id)
    {
        selenium.click(getActionId("enable", id));
    }

    public void clickUninstall(String id)
    {
        selenium.click(getActionId("uninstall", id));
    }

    public String getActionId(String action, String id)
    {
        return action + ":" + id;
    }
}
