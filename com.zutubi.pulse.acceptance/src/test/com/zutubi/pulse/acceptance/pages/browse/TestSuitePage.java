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
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * The tests page for a specific suite result.
 */
public class TestSuitePage extends AbstractTestsPage
{
    public static final String FILTER_NONE = "";
    public static final String FILTER_BROKEN = "broken";

    private static final String ID_COMBO = "filter-combo";

    private String projectName;
    private String stageName;
    private String suitePath;
    private long buildId;

    public TestSuitePage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, String stageName, String suitePath)
    {
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-tests-" + stageName + "-" + suitePath, "build " + buildId);
        this.projectName = projectName;
        this.stageName = stageName;
        this.suitePath = suitePath;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.stageTests(WebUtils.uriComponentEncode(projectName), Long.toString(buildId), stageName) + suitePath;
    }

    public TestSuitePage clickSuiteAndWait(String suiteName)
    {
        clickSuiteLink(suiteName);
        return browser.waitFor(TestSuitePage.class, projectName, buildId, stageName, PathUtils.getPath(suitePath, WebUtils.uriComponentEncode(suiteName)));
    }

    public String getCurrentFilter()
    {
        return (String) browser.evaluateScript("return Ext.getCmp('" + ID_COMBO + "').getValue();");
    }

    public void setFilterAndWait(String filter)
    {
        browser.setComboByValue(ID_COMBO, filter);
        browser.waitForCondition("return filtering === false");
    }

    public boolean isTestCaseVisible(String caseName)
    {
        return browser.isVisible(By.xpath("//td[text()='" + caseName + "']"));
    }
}