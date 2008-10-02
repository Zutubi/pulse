package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.browse.BuildArtifactsPage;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The artifacts tab for a personal build result.
 */
public class PersonalBuildArtifactsPage extends BuildArtifactsPage
{
    private long buildId;

    public PersonalBuildArtifactsPage(Selenium selenium, Urls urls, long buildId)
    {
        super(selenium, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildArtifacts(Long.toString(buildId));
    }
}
