package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import static com.zutubi.util.StringUtils.toValidHtmlName;

/**
 * The tests page for a specific suite result.
 */
public class TestSuitePage extends AbstractTestsPage
{
    private String projectName;
    private String stageName;
    private String suitePath;
    private long buildId;

    public TestSuitePage(Selenium selenium, Urls urls, String projectName, long buildId, String stageName, String suitePath)
    {
        super(selenium, urls, toValidHtmlName(projectName + "-build-" + Long.toString(buildId) + "-tests-" + stageName + "-" + suitePath), "build " + buildId);
        this.projectName = projectName;
        this.stageName = stageName;
        this.suitePath = suitePath;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.stageTests(projectName, Long.toString(buildId), stageName) + suitePath;
    }

    public TestSuitePage clickSuiteAndWait(String suiteName)
    {
        clickSuiteLink(suiteName);
        TestSuitePage page = new TestSuitePage(selenium, urls, projectName, buildId, stageName, PathUtils.getPath(suitePath, StringUtils.uriComponentEncode(suiteName)));
        page.waitFor();
        return page;
    }
}