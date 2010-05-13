package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.user.ResponsibilityModel;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.WebUtils;

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

    public void hideGroupAndWait(String group)
    {
        // Under IE for some unknown reason selenium cannot find the group
        // hide element.  I have verified it exists with the expected id, and
        // that it is not a waiting issue.  So I work around this case, but
        // continue to test the link itself on non-Windows systems.
        if (SystemUtils.IS_WINDOWS)
        {
            browser.open(urls.base() + "user/hideDashboardGroup.action?groupName=" + WebUtils.formUrlEncode(group));
        }
        else
        {
            clickGroupAction(group, ACTION_HIDE);
        }
        browser.waitForPageToLoad();
        waitFor();
    }

    public void hideProjectAndWait(String group, String project)
    {
        // As above, a workaround for Selenium/IE issues.
        if (SystemUtils.IS_WINDOWS)
        {
            browser.open(urls.base() + "user/hideDashboardProject.action?projectName=" + WebUtils.formUrlEncode(project));
        }
        else
        {
            clickProjectAction(group, project, ACTION_HIDE);
        }
        browser.waitForPageToLoad();
        waitFor();
    }

    public boolean hasResponsibilities()
    {
        return browser.isElementIdPresent(ID_RESPONSIBILITIES);
    }

    public boolean hasResponsibility(String project)
    {
        return browser.isElementIdPresent(ResponsibilityModel.getResponsibilityId(project));
    }

    public void clearResponsibility(String project)
    {
        browser.click(getClearResponsibilityId(project));
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
        return "clear-" + ResponsibilityModel.getResponsibilityId(project);
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
