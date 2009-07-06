package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import static com.zutubi.util.StringUtils.uriComponentEncode;

/**
 * The tests page for a stage result.
 */
public class StageTestsPage extends AbstractTestsPage
{
    private String projectName;
    private String stageName;
    private long buildId;

    public StageTestsPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, String stageName)
    {
        super(browser, urls, uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-tests-" + uriComponentEncode(stageName), "build " + buildId);
        this.projectName = projectName;
        this.stageName = stageName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.stageTests(projectName, Long.toString(buildId), stageName);
    }

    public TestSuitePage clickSuiteAndWait(String suite)
    {
        clickSuiteLink(suite);
        return browser.waitFor(TestSuitePage.class, projectName, buildId, stageName, suite);
    }

    public boolean isLoadFailureMessageShown()
    {
        return browser.isElementIdPresent("test.load.failure");
    }

    public boolean isBreadcrumbsVisible()
    {
        return browser.isElementIdPresent("allcrumb") && browser.isElementIdPresent("stagecrumb");
    }
}