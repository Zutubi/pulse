package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
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

    public BuildTestsPage(Selenium selenium, Urls urls, String projectName, long buildId)
    {
        super(selenium, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-tests", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildTests(projectName, Long.toString(buildId));
    }

    public boolean isBuildComplete()
    {
        return !selenium.isElementPresent(ID_BUILD_INCOMPLETE);
    }

    public boolean hasTests()
    {
        return isBuildComplete() && !selenium.isElementPresent(ID_NO_TESTS) && selenium.isElementPresent(ID_TEST_SUMMARY);
    }

    public StageTestsPage clickStageAndWait(String stage)
    {
        selenium.click("stage-" + stage);
        StageTestsPage stagePage = new StageTestsPage(selenium, urls, projectName, buildId, stage);
        stagePage.waitFor();
        return stagePage;
    }
}
