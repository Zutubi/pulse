package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The summary tab for a personal build result.
 */
public class PersonalBuildSummaryPage extends BuildSummaryPage
{
    private long buildId;

    public PersonalBuildSummaryPage(Selenium selenium, Urls urls, long buildId)
    {
        super(selenium, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuild(Long.toString(buildId));
    }
}
