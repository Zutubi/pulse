package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import junit.framework.Assert;

/**
 * The projects page is the default in the browse section and shows a list of
 * projects, including the latest build result of each.
 */
public class ProjectsPage extends SeleniumPage
{
    public ProjectsPage(Selenium selenium)
    {
        super(selenium, "projects", "projects");
    }

    public String getUrl()
    {
        return "/viewProjects.action";
    }

    public void assertProjectPresent(String name)
    {
        Assert.assertTrue(selenium.isElementPresent(name));
    }

    public void triggerProject(String name)
    {
        selenium.click("trigger-" + name);
    }
}
