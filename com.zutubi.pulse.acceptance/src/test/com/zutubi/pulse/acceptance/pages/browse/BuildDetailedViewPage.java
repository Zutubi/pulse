package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The detailed view tab for a build result.
 */
public class BuildDetailedViewPage extends SeleniumPage
{
    private String projectName;
    private long buildId;
    
    public BuildDetailedViewPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-detailed", StringUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildDetails(projectName, Long.toString(buildId));
    }

    public void clickCommand(String stageName, String commandName)
    {
        browser.click("xpath=//li[@id='stage-" + stageName + "-command-" + commandName + "']/a");
    }

    /**
     * Test whether or not the build log link is available on the page.
     *
     * @return true if the build log link is available, false otherwise.
     */
    public boolean isBuildLogLinkPresent()
    {
        return browser.isLinkPresent(getLogLinkId());
    }

    public void clickBuildLogLink()
    {
        browser.click("id=" + getLogLinkId());
        browser.waitForPageToLoad();
    }

    private String getLogLinkId()
    {
        return "log-" + projectName + "-" + buildId;
    }
}
