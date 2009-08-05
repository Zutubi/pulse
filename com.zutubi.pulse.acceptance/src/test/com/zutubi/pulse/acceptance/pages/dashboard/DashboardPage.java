package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
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

    private String getClearResponsibilityId(String project)
    {
        return "clear-" + ResponsibilityModel.getResponsibilityId(project);
    }
}
