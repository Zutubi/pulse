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

package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.user.UserResponsibilityModel;
import com.zutubi.util.Condition;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * The dashboard page shows a user's configurable dashboard.
 */
public class DashboardPage extends ProjectsSummaryPage
{
    private static final String ID_RESPONSIBILITIES = "responsibilities";
    private static final String ID_PROJECT_CHANGES = "project-changes-table";

    public DashboardPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "dashboard-content", "dashboard");
    }

    public String getUrl()
    {
        return urls.dashboard();
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("view.initialised");
    }

    public void hideGroupAndWait(final String group)
    {
        // Under IE for some unknown reason selenium cannot find the group
        // hide element.  I have verified it exists with the expected id, and
        // that it is not a waiting issue.  So I work around this case, but
        // continue to test the link itself on non-Windows systems.
        if (SystemUtils.IS_WINDOWS)
        {
            browser.evaluateScript("hideDashboardGroup('" + WebUtils.formUrlEncode(group) + "')");
        }
        else
        {
            clickGroupAction(group, ACTION_HIDE);
        }

        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !isGroupPresent(group);
            }
        }, SeleniumBrowser.DEFAULT_TIMEOUT, "group to be hidden");
    }

    public void hideProjectAndWait(final String group, final String project)
    {
        // As above, a workaround for Selenium/IE issues.
        if (SystemUtils.IS_WINDOWS)
        {
            browser.evaluateScript("hideDashboardProject('" + WebUtils.formUrlEncode(project) + "')");
        }
        else
        {
            clickProjectAction(group, project, ACTION_HIDE);
        }

        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !isUngroupedProjectPresent(project);
            }
        }, SeleniumBrowser.DEFAULT_TIMEOUT, "project to be hidden");
    }

    public boolean hasResponsibilities()
    {
        return browser.isElementIdPresent(ID_RESPONSIBILITIES);
    }

    public boolean hasResponsibility(String project)
    {
        return browser.isElementIdPresent(UserResponsibilityModel.getResponsibilityId(project));
    }

    public void clearResponsibility(String project)
    {
        browser.click(By.id(getClearResponsibilityId(project)));
    }

    public boolean isClearResponsibilityPresent(String project)
    {
        return browser.isElementIdPresent(getClearResponsibilityId(project));
    }

    public ProjectChange getProjectChange(int index)
    {
        int row = index + 2;
        return new ProjectChange(
                browser.getCellContents(ID_PROJECT_CHANGES, row, 0),
                browser.getCellContents(ID_PROJECT_CHANGES, row, 1),
                browser.getCellContents(ID_PROJECT_CHANGES, row, 3),
                ResultState.fromPrettyString(browser.getCellContents(ID_PROJECT_CHANGES, row, 4)),
                browser.getCellContents(ID_PROJECT_CHANGES, row, 5)
        );
    }
    
    private String getClearResponsibilityId(String project)
    {
        return "clear-" + UserResponsibilityModel.getResponsibilityId(project);
    }
    
    public static class ProjectChange
    {
        public String revision;
        public String author;
        public String comment;
        public ResultState status;
        public String builds;

        public ProjectChange(String revision, String author, String comment, ResultState status, String builds)
        {
            this.revision = revision;
            this.author = author;
            this.comment = comment;
            this.status = status;
            this.builds = builds;
        }
    }
}
