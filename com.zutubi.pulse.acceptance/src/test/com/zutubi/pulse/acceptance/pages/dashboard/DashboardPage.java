package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The dashboard page shows a user's configurable dashboard.
 */
public class DashboardPage extends ProjectsSummaryPage
{
    public DashboardPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "dashboard-panel", "dashboard");
    }

    public String getUrl()
    {
        return urls.dashboard();
    }

    public void hideGroupAndWait(String group)
    {
        clickGroupAction(group, ACTION_HIDE);
        selenium.waitForPageToLoad("30000");
    }

    public void hideProjectAndWait(String group, String project)
    {
        clickProjectAction(group, project, ACTION_HIDE);
        selenium.waitForPageToLoad("30000");
    }
}
