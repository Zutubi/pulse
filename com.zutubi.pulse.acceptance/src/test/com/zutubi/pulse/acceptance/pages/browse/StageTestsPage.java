package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
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

    public StageTestsPage(Selenium selenium, Urls urls, String projectName, long buildId, String stageName)
    {
        super(selenium, urls, uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-tests-" + uriComponentEncode(stageName), "build " + buildId);
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
        TestSuitePage page = new TestSuitePage(selenium, urls, projectName, buildId, stageName, suite);
        page.waitFor();
        return page;
    }
}