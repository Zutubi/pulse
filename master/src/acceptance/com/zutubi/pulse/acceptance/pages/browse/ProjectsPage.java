package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import junit.framework.Assert;

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
        Assert.assertTrue(selenium.isElementPresent(name));
    }

    public void triggerProject(String name)
    {
        selenium.click("trigger-" + name);
    }
}
