package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The detailed view tab for a personal build result.
 */
public class PersonalBuildDetailedViewPage extends BuildDetailedViewPage
{
    private long buildId;

    public PersonalBuildDetailedViewPage(Selenium selenium, Urls urls, long buildId)
    {
        super(selenium, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildDetails(Long.toString(buildId));
    }
}
