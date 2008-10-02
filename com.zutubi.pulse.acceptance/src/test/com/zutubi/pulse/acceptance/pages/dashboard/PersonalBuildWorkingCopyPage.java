package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.browse.BuildWorkingCopyPage;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The working copy tab for a personal build result.
 */
public class PersonalBuildWorkingCopyPage extends BuildWorkingCopyPage
{
    private long buildId;

    public PersonalBuildWorkingCopyPage(Selenium selenium, Urls urls, long buildId)
    {
        super(selenium, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildWorkingCopy(Long.toString(buildId));
    }
}
