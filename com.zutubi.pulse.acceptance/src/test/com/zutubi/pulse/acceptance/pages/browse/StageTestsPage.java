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
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

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
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-tests-" + stageName, "build " + buildId);
        this.projectName = projectName;
        this.stageName = stageName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.stageTests(WebUtils.uriComponentEncode(projectName), Long.toString(buildId), stageName);
    }

    public TestSuitePage clickSuiteAndWait(String suite)
    {
        clickSuiteLink(suite);
        return browser.waitFor(TestSuitePage.class, projectName, buildId, stageName, WebUtils.uriComponentEncode(suite));
    }

    public boolean isLoadFailureMessageShown()
    {
        return browser.isElementIdPresent("test-load-failure");
    }

    public boolean isBreadcrumbsVisible()
    {
        return browser.isElementIdPresent("allcrumb") && browser.isElementIdPresent("stagecrumb");
    }
}