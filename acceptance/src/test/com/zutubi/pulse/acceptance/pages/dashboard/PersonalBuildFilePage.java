package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.browse.BuildFilePage;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The pulse file tab for a personal build result.
 */
public class PersonalBuildFilePage extends BuildFilePage
{
    private long buildId;

    public PersonalBuildFilePage(Selenium selenium, Urls urls, long buildId)
    {
        super(selenium, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildFile(Long.toString(buildId));
    }
}
