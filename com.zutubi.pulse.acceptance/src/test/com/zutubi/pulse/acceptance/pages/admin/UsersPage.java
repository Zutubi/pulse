package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.USERS_SCOPE;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * Simple specialisation of a list page for the admin/users tab.
 */
public class UsersPage extends ListPage
{
    private static final String STATE_ACTIVE_COUNT = "activeCount";

    public UsersPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, USERS_SCOPE);
    }

    public boolean isActiveCountPresent()
    {
        return isStateFieldPresent(STATE_ACTIVE_COUNT);
    }

    public String getActiveCount()
    {
        return getStateField(STATE_ACTIVE_COUNT);
    }
}
