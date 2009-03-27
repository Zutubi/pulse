package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;

/**
 * Special override of {@link com.zutubi.pulse.acceptance.pages.admin.CompositePage}
 * that handles the differences of the user preferences.
 */
public class PreferencesPage extends CompositePage
{
    public PreferencesPage(Selenium selenium, Urls urls, String user)
    {
        super(selenium, urls, PathUtils.getPath(ConfigurationRegistry.USERS_SCOPE, user, "preferences"));
    }

    @Override
    public String getUrl()
    {
        return urls.dashboardPreferences();
    }
}
