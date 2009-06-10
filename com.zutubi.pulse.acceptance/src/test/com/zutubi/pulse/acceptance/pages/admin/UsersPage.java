package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * Simple specialisation of a list page for the admin/users tab.
 */
public class UsersPage extends ListPage
{
    public UsersPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, MasterConfigurationRegistry.USERS_SCOPE);
    }
}
