package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The summary tab for a personal build result.
 */
public class PersonalBuildSummaryPage extends BuildSummaryPage
{
    private long buildNumber;

    public PersonalBuildSummaryPage(SeleniumBrowser browser, Urls urls, long buildNumber)
    {
        super(browser, urls, "personal", buildNumber);
        this.buildNumber = buildNumber;
    }

    public String getUrl()
    {
        return urls.myBuild(Long.toString(buildNumber));
    }
}
