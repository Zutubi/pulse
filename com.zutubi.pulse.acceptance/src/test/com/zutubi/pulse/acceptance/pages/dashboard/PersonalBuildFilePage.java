package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.BuildFilePage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The pulse file tab for a personal build result.
 */
public class PersonalBuildFilePage extends BuildFilePage
{
    private long buildId;

    public PersonalBuildFilePage(SeleniumBrowser browser, Urls urls, long buildId)
    {
        super(browser, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildFile(Long.toString(buildId));
    }
}
