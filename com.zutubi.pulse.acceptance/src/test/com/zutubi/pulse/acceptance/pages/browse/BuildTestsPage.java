/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

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

    public TestResultSummary getTestSummary()
    {
        int total = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 2, 1));
        int failures = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 3, 1));
        int errors = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 4, 1));
        int expectedFailures = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 5, 1));
        int skipped = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 6, 1));

        return new TestResultSummary(expectedFailures, errors, failures, skipped, total);
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
        browser.click(By.xpath(getStageLinkXPath(stage)));
        return browser.waitFor(StageTestsPage.class, projectName, buildId, stage);
    }

    public boolean isStagePresent(String stage)
    {
        return browser.isElementIdPresent("stage-" + stage);
    }

    public boolean isStageLinkPresent(String stage)
    {
        return browser.isElementPresent(By.xpath(getStageLinkXPath(stage)));
    }

    private String getStageLinkXPath(String stage)
    {
        return "//td[@id='stage-" + stage + "']/a";
    }

    public boolean isStageComplete(String stage)
    {
        return !browser.isElementIdPresent("stage-" + stage + "-inprogress");
    }
}
