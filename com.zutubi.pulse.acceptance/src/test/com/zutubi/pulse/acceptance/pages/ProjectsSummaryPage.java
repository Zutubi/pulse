package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * Base class for pages that show projects with their latest status (e.g. the
 * browse and dashboard views).
 */
public abstract class ProjectsSummaryPage extends SeleniumPage
{
    public static final String ACTION_HIDE = "hide";

    public ProjectsSummaryPage(SeleniumBrowser browser, Urls urls, String id, String title)
    {
        super(browser, urls, id, title);
    }

    public boolean isUngroupedProjectPresent(String name)
    {
        return browser.isElementIdPresent(getProjectRowId(null, name));
    }

    public boolean isProjectPresent(String group, String name)
    {
        return browser.isElementIdPresent(getProjectRowId(group, name));
    }

    public boolean isGroupPresent(String group)
    {
        return browser.isElementIdPresent(getGroupId(group));
    }

    public String getProjectMenuId(String group, String project)
    {
        return getProjectRowId(group, project) + "-actions";
    }

    public String getProjectActionId(String group, String project, String action)
    {
        return action + "-" + getProjectMenuId(group, project);
    }

    public void clickProjectAction(String group, String project, String action)
    {
        browser.click(getProjectMenuId(group, project) + "-link");
        browser.waitAndClick(getProjectActionId(group, project, action));
    }

    public String getGroupActionId(String group, String action)
    {
        return action + "." + getGroupId(group);
    }

    public boolean isGroupActionPresent(String group, String action)
    {
        return browser.isElementIdPresent(getGroupActionId(group, action));
    }

    public void clickGroupAction(String group, String action)
    {
        browser.click(getGroupActionId(group, action));
    }

    public String getGroupId(String group)
    {
        if(group == null)
        {
            return "ungroup";
        }
        else
        {
            return "group." + group;
        }
    }

    public String getProjectRowId(String group, String name)
    {
        String prefix;
        if(group == null)
        {
            prefix = "ungrouped";
        }
        else
        {
            prefix = "grouped." + group;
        }

        return prefix + "." + name;
    }

    public String getBuildingSummary(String group, String templateName)
    {
        String id = getProjectRowId(group, templateName) + ".building";
        browser.waitForElement(id);
        return browser.getText(id);
    }

    public String getBuildLinkId(String group, String projectName, int buildIndex)
    {
        return "b" + (buildIndex + 1) + "." + getProjectRowId(group, projectName) + "-link";
    }

    public boolean isResponsibilityPresent(String group, String project)
    {
        return browser.isElementIdPresent(getProjectRowId(group, project) + "_fixing");
    }
}
