package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The dashboard page shows a user's configurable dashboard.
 */
public class DashboardPage extends SeleniumPage
{
    public DashboardPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "dashboard-panel", "dashboard");
    }

    public String getUrl()
    {
        return urls.dashboard();
    }
}
