package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The browse page is the default in the browse section and shows a list of
 * projects, including the latest build results of each.
 */
public class BrowsePage extends SeleniumPage
{
    public BrowsePage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "projects", "projects");
    }

    public String getUrl()
    {
        return urls.projects();
    }

    public void assertProjectPresent(String name)
    {
        SeleniumUtils.assertElementPresent(selenium, getProjectRowId(null, name));
    }

    public void assertProjectPresent(String group, String name)
    {
        SeleniumUtils.assertElementPresent(selenium, getProjectRowId(group, name));
    }

    public void assertProjectNotPresent(String group, String name)
    {
        SeleniumUtils.assertElementNotPresent(selenium, getProjectRowId(group, name));
    }

    public void assertGroupPresent(String group, String... projects)
    {
        SeleniumUtils.assertElementPresent(selenium, getGroupId(group));
        for(String project: projects)
        {
            assertProjectPresent(group, project);
        }
    }

    public void assertGroupNotPresent(String group)
    {
        SeleniumUtils.assertElementNotPresent(selenium, getGroupId(group));
    }

    private String getGroupId(String group)
    {
        if(group == null)
        {
            return "group.ungrouped.projects";
        }
        else
        {
            return "group." + group;
        }
    }

    private String getProjectRowId(String group, String name)
    {
        String prefix;
        if(group == null)
        {
            prefix = "ungrouped.ungrouped.projects";
        }
        else
        {
            prefix = "grouped." + group;
        }

        return prefix + "." + name;
    }

    public void triggerProject(String name)
    {
        selenium.click("trigger-" + name);
    }
}
