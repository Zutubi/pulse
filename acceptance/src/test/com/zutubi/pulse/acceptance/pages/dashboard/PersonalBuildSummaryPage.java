package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The summary tab for a personal build result.
 */
public class PersonalBuildSummaryPage extends SeleniumPage
{
    private long buildId;

    public PersonalBuildSummaryPage(Selenium selenium, Urls urls, long buildId)
    {
        super(selenium, urls, "personal-build-" + Long.toString(buildId) + "-summary", "build " + buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuild(Long.toString(buildId));
    }
}
