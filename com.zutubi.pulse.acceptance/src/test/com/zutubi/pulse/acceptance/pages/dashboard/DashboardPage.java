package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The dashboard page shows a user's configurable dashboard.
 */
public class DashboardPage extends SeleniumPage
{
    public DashboardPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "dashboard-panel", "dashboard");
    }

    public String getUrl()
    {
        return urls.dashboard();
    }

    public String getGroupId(String group)
    {
        return "group_" + group;
    }

    public String getGroupedProjectId(String group, String project)
    {
        return getGroupId(group) + "_" + getUngroupedProjectId(project);
    }

    public String getUngroupedProjectId(String project)
    {
        return "project_" + project;
    }

    public boolean isGroupPresent(String group)
    {
        return selenium.isElementPresent(getGroupId(group));
    }

    public boolean isGroupedProjectPresent(String group, String project)
    {
        return selenium.isElementPresent(getGroupedProjectId(group, project));
    }

    public boolean isUngroupedProjectPresent(String project)
    {
        return selenium.isElementPresent(getUngroupedProjectId(project));
    }

    public String getHideGroupLinkId(String group)
    {
        return "hide_" + getGroupId(group);
    }

    public void hideGroupAndWait(String group)
    {
        selenium.click(getHideGroupLinkId(group));
        selenium.waitForPageToLoad("30000");
    }

    public String getHideProjectLinkId(String project)
    {
        return "hide_" + getUngroupedProjectId(project);
    }

    public void hideProjectAndWait(String project)
    {
        selenium.click(getHideProjectLinkId(project));
        selenium.waitForPageToLoad("30000");
    }
}
