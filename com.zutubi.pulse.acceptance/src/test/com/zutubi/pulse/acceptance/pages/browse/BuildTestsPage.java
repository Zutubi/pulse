package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The tests tab for a build result.
 */
public class BuildTestsPage extends AbstractTestsPage
{
    private static final String ID_BUILD_INCOMPLETE = "build.incomplete";
    private static final String ID_NO_TESTS = "no.tests";

    private String projectName;
    private long buildId;

    public BuildTestsPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-tests", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildTests(projectName, Long.toString(buildId));
    }

    public boolean isBuildComplete()
    {
        return !browser.isElementIdPresent(ID_BUILD_INCOMPLETE);
    }

    public boolean isFailureUnavailableMessageShown()
    {
        return  browser.isElementIdPresent("test.broken.unavailable");
    }

    public boolean hasFailedTests()
    {
        return browser.isElementIdPresent("failed.tests");
    }

    public boolean hasTests()
    {
        return isBuildComplete() && !browser.isElementIdPresent(ID_NO_TESTS) && browser.isElementIdPresent(ID_TEST_SUMMARY);
    }

    public StageTestsPage clickStageAndWait(String stage)
    {
        browser.click("stage-" + stage);
        return browser.waitFor(StageTestsPage.class, projectName, buildId, stage);
    }
}
