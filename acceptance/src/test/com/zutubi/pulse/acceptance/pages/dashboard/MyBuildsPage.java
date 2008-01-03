package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.StringUtils;
import com.thoughtworks.selenium.Selenium;

/**
 * The my builds page shows a user's personal builds.
 */
public class MyBuildsPage extends SeleniumPage
{
    public MyBuildsPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "personal-builds", "my builds");
    }

    public String getUrl()
    {
        return urls.dashboardMyBuilds();
    }
}
