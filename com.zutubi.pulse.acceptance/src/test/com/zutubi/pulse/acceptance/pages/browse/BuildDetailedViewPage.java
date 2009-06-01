package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The detailed view tab for a build result.
 */
public class BuildDetailedViewPage extends SeleniumPage
{
    private String projectName;
    private long buildId;
    
    public BuildDetailedViewPage(Selenium selenium, Urls urls, String projectName, long buildId)
    {
        super(selenium, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-detailed", StringUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildDetails(projectName, Long.toString(buildId));
    }

    public void clickCommand(String stageName, String commandName)
    {
        selenium.click("xpath=//li[@id='stage-" + stageName + "-command-" + commandName + "']/a");
    }

    /**
     * Test whether or not the build log link is available on the page.
     *
     * @return true if the build log link is available, false otherwise.
     */
    public boolean isBuildLogLinkPresent()
    {
        String logLinkId = getLogLinkId();
        return SeleniumUtils.isLinkPresent(selenium, logLinkId);
    }

    public void clickBuildLogLink()
    {
        selenium.click("id=" + getLogLinkId());
        selenium.waitForPageToLoad("10000");
    }

    private String getLogLinkId()
    {
        return "log-" + projectName + "-" + buildId;
    }
}
