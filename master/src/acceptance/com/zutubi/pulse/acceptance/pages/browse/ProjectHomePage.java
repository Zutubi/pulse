package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import junit.framework.Assert;

/**
 * The project home page is a summary of the state and recent activity for a
 * project.
 */
public class ProjectHomePage extends SeleniumPage
{
    public ProjectHomePage(Selenium selenium, String projectName)
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
