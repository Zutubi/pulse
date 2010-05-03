package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailsPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The details tab for a personal build result.
 */
public class PersonalBuildDetailsPage extends BuildDetailsPage
{
    private long buildId;

    public PersonalBuildDetailsPage(SeleniumBrowser browser, Urls urls, long buildId)
    {
        super(browser, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildDetails(Long.toString(buildId));
    }
}
