package com.zutubi.pulse.acceptance.pages;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * Base class for pages that show projects with their latest status (e.g. the
 * browse and dashboard views).
 */
public abstract class ProjectsSummaryPage extends SeleniumPage
{
    public static final String ACTION_HIDE = "hide";

    public ProjectsSummaryPage(Selenium selenium, Urls urls, String id, String title)
    {
        super(selenium, urls, id, title);
    }

    public boolean isUngroupedProjectPresent(String name)
    {
        return selenium.isElementPresent(getProjectRowId(null, name));
    }

    public boolean isProjectPresent(String group, String name)
    {
        return selenium.isElementPresent(getProjectRowId(group, name));
    }

    public boolean isGroupPresent(String group)
    {
        return selenium.isElementPresent(getGroupId(group));
    }

    public String getProjectMenuId(String group, String project)
    {
        return getProjectRowId(group, project) + "_actions";
    }

    public String getProjectActionId(String group, String project, String action)
    {
        return action + "-" + getProjectMenuId(group, project);
    }

    public void clickProjectAction(String group, String project, String action)
    {
        selenium.click(getProjectMenuId(group, project) + "_link");
        SeleniumUtils.waitAndClickId(selenium, getProjectActionId(group, project, action));
    }

    public String getGroupActionId(String group, String action)
    {
        return action + "." + getGroupId(group);
    }

    public void clickGroupAction(String group, String action)
    {
        selenium.click(getGroupActionId(group, action));
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
        SeleniumUtils.waitForElementId(selenium, id);
        return selenium.getText(id);
    }

    public boolean isResponsibilityPresent(String group, String project, int buildRowNumber)
    {
        return selenium.isElementPresent("b" + buildRowNumber + "." + getProjectRowId(group, project) + "_fixing");
    }
}
