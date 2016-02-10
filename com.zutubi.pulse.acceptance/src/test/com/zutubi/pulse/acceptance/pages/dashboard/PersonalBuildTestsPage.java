package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.BuildTestsPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The tests tab for a personal build result.
 */
public class PersonalBuildTestsPage extends BuildTestsPage
{
    private long buildId;

    public PersonalBuildTestsPage(SeleniumBrowser browser, Urls urls, long buildId)
    {
        super(browser, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.myBuildTests(Long.toString(buildId));
    }
}
