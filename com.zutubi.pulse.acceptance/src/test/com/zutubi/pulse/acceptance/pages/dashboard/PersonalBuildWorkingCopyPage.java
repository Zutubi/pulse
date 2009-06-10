package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.BuildWorkingCopyPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The working copy tab for a personal build result.
 */
public class PersonalBuildWorkingCopyPage extends BuildWorkingCopyPage
{
    private long buildId;

    public PersonalBuildWorkingCopyPage(SeleniumBrowser browser, Urls urls, long buildId)
    {
        super(browser, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildWorkingCopy(Long.toString(buildId));
    }
}
