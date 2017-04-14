/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.InstallPluginForm;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import org.openqa.selenium.By;

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
        return browser.getText(By.id("status:" + id));
    }

    public PluginPage clickPlugin(String id)
    {
        browser.click(By.id("select:" + id));
        return browser.createPage(PluginPage.class, id);
    }

    public InstallPluginForm clickInstall()
    {
        browser.click(By.id("plugin.add"));
        return new InstallPluginForm(browser);
    }

    public void clickDisable(String id)
    {
        browser.click(By.id(getActionId("disable", id)));
    }

    public void clickEnable(String id)
    {
        browser.click(By.id(getActionId("enable", id)));
    }

    public void clickUninstall(String id)
    {
        browser.click(By.id(getActionId("uninstall", id)));
    }

    public String getActionId(String action, String id)
    {
        return action + ":" + id;
    }
}
