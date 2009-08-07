package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * The detailed view tab for a build result.
 */
public class BuildDetailedViewPage extends SeleniumPage
{
    private String projectName;
    private long buildId;
    
    public BuildDetailedViewPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, WebUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-detailed", WebUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildDetails(projectName, Long.toString(buildId));
    }

    public void clickStage(String stageName)
    {
        browser.click("xpath=//li[@id='stage-" + stageName + "']/a");
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
        return browser.isLinkPresent(getBuildLogLinkId());
    }

    public BuildLogPage clickBuildLogLink()
    {
        browser.click("id=" + getBuildLogLinkId());
        browser.waitForPageToLoad();
        return browser.createPage(BuildLogPage.class, projectName, buildId);
    }

    private String getBuildLogLinkId()
    {
        return "log-" + projectName + "-" + buildId;
    }

    public boolean isStageLogLinkPresent(String stageName)
    {
        return browser.isLinkPresent(getStageLogLinkId(stageName));
    }

    public StageLogPage clickStageLogLink(String stageName)
    {
        browser.click("id=" + getStageLogLinkId(stageName));
        browser.waitForPageToLoad();
        return browser.createPage(StageLogPage.class, projectName, buildId, stageName);
    }

    private String getStageLogLinkId(String stageName)
    {
        return "log-" + projectName + "-" + buildId + "-" + stageName;
    }

    public String getCustomFieldsId(String stageName)
    {
        String id = "custom.fields";
        if (stageName != null)
        {
            id += "." + stageName;
        }

        return id;
    }

    public String getCustomFieldValue(String stageName, int index)
    {
        return browser.getCellContents(getCustomFieldsId(stageName), index + 1, 1);
    }
}
