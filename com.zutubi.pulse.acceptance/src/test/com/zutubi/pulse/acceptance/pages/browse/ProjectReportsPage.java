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
import org.openqa.selenium.By;

/**
 * The project reports page shows trend reports for a project.
 */
public class ProjectReportsPage extends ResponsibilityPage
{
    private String projectName;
    private String group;

    public ProjectReportsPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        this(browser, urls, projectName, null);
    }

    public ProjectReportsPage(SeleniumBrowser browser, Urls urls, String projectName, String group)
    {
        super(browser, urls, getId(projectName, group), projectName);
        this.projectName = projectName;
        this.group = group;
    }

    private static String getId(String projectName, String group)
    {
        String id = "project-reports-" + projectName;
        if (group != null)
        {
            id += "-" + group;
        }

        return id;
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel.loaded");
    }

    public String getUrl()
    {
        if (group == null)
        {
            return urls.projectReports(projectName);
        }
        else
        {
            return urls.projectReports(projectName, group);
        }
    }

    public void clickApply()
    {
        browser.click(By.id("reports-apply"));
    }
}