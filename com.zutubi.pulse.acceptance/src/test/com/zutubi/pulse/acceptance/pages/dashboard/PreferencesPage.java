package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;

import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.USERS_SCOPE;

/**
 * The user's preferences page: configuration for the user under their dashboard.
 */
public class PreferencesPage extends CompositePage
{
    public PreferencesPage(SeleniumBrowser browser, Urls urls, String user)
    {
        super(browser, urls, PathUtils.getPath(USERS_SCOPE, user, "preferences"));
    }

    @Override
    public String getUrl()
    {
        return urls.preferences();
    }
}
