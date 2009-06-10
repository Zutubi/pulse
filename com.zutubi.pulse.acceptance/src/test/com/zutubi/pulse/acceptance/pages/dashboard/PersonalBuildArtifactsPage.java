package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.BuildArtifactsPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The artifacts tab for a personal build result.
 */
public class PersonalBuildArtifactsPage extends BuildArtifactsPage
{
    private long buildId;

    public PersonalBuildArtifactsPage(SeleniumBrowser browser, Urls urls, long buildId)
    {
        super(browser, urls, "personal", buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildArtifacts(Long.toString(buildId));
    }
}
