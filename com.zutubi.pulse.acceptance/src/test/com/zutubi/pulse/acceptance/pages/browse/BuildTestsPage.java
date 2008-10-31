package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The tests tab for a build result.
 */
public class BuildTestsPage extends SeleniumPage
{
    private String projectName;
    private long buildId;

    public BuildTestsPage(Selenium selenium, Urls urls, String projectName, long buildId)
    {
        super(selenium, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-tests", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildTests(projectName, Long.toString(buildId));
    }

    public boolean isBuildComplete()
    {
        return !selenium.isElementPresent("build.incomplete");
    }

    public boolean hasTests()
    {
        return isBuildComplete() && !selenium.isElementPresent("no.tests");
    }
}
