package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The projects page is the default in the browse section and shows a list of
 * projects, including the latest build result of each.
 */
public class ProjectsPage extends SeleniumPage
{
    public ProjectsPage(Selenium selenium, Urls urls)
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
            group = "_ungrouped";
        }
        return "group." + group;
    }

    private String getProjectRowId(String group, String name)
    {
        return getGroupId(group) + ".project." + name;
    }

    public void triggerProject(String name)
    {
        selenium.click("trigger-" + name);
    }
}
