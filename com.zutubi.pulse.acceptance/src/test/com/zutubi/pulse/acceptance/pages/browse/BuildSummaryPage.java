package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The summary tab for a build result.
 */
public class BuildSummaryPage extends ResponsibilityPage
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

    private String getHookId(String hookName)
    {
        return "hook." + hookName;
    }

    public boolean isHookPresent(String hookName)
    {
        return selenium.isElementPresent(getHookId(hookName));
    }

    public void clickHook(String hookName)
    {
        selenium.click(getHookId(hookName));
    }

    public boolean hasTests()
    {
        return !getSummaryTestsColumnText().contains("none");
    }

    public String getSummaryTestsColumnText()
    {
        return selenium.getText("id="+projectName+".build."+buildId+".test");
    }
}
