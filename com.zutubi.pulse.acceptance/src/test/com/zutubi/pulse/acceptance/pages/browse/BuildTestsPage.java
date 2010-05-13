package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * The tests tab for a build result.
 */
public class BuildTestsPage extends AbstractTestsPage
{
    private String projectName;
    private long buildId;

    public BuildTestsPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-tests", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildTests(WebUtils.uriComponentEncode(projectName), Long.toString(buildId));
    }

    public boolean isFailureUnavailableMessageShown()
    {
        return  browser.isElementIdPresent("test.broken.unavailable");
    }

    public boolean hasFailedTests()
    {
        return browser.isElementIdPresent("failed-tests");
    }

    public boolean hasFailedTestsForStage(String stage)
    {
        return browser.isElementIdPresent("stage-" + stage + "-failed");
    }
    
    public StageTestsPage clickStageAndWait(String stage)
    {
        browser.click("stage-" + stage);
        return browser.waitFor(StageTestsPage.class, projectName, buildId, stage);
    }

    public boolean isStagePresent(String stage)
    {
        return browser.isElementIdPresent("stage-" + stage);
    }

    public boolean isStageComplete(String stage)
    {
        return !browser.isElementIdPresent("stage-" + stage + "-inprogress");
    }
}
