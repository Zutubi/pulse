package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The summary tab for a personal build result.
 */
public class PersonalBuildSummaryPage extends BuildSummaryPage
{
    private long buildId;

    public PersonalBuildSummaryPage(SeleniumBrowser browser, Urls urls, long buildId)
    {
        super(browser, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuild(Long.toString(buildId));
    }
}
