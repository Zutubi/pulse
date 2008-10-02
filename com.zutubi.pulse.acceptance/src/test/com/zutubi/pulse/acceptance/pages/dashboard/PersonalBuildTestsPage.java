package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.browse.BuildTestsPage;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The tests tab for a personal build result.
 */
public class PersonalBuildTestsPage extends BuildTestsPage
{
    private long buildId;

    public PersonalBuildTestsPage(Selenium selenium, Urls urls, long buildId)
    {
        super(selenium, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildTests(Long.toString(buildId));
    }
}
