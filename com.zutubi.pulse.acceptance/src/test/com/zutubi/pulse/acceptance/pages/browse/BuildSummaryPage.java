package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.StringUtils;

/**
 * The summary tab for a build result.
 */
public class BuildSummaryPage extends SeleniumPage
{
    private String projectName;
    private long buildId;

    public BuildSummaryPage(Selenium selenium, Urls urls, String projectName, long buildId)
    {
        super(selenium, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-summary", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildSummary(projectName, Long.toString(buildId));
    }

    public void clickHook(String hookName)
    {
        selenium.click("hook." + hookName);
    }
}
