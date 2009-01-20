package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * Simple specialisation of a list page for the admin/users tab.
 */
public class UsersPage extends ListPage
{
    public UsersPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, MasterConfigurationRegistry.USERS_SCOPE);
    }
}
