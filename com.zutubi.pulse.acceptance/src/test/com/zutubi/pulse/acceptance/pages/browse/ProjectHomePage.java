package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The project home page is a summary of the state and recent activity for a
 * project.
 */
public class ProjectHomePage extends SeleniumPage
{
    private String projectName;

    public ProjectHomePage(Selenium selenium, Urls urls, String projectName)
    {
        super(selenium, urls, "project-home-" + StringUtils.uriComponentEncode(projectName), StringUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
    }

    public String getActionLinkId(String action)
    {
        return "action." + action;
    }

    public void clickAction(String action)
    {
        selenium.click(getActionLinkId(action));
    }

    public void triggerBuild()
    {
        clickAction("trigger");
    }
    
    public String getUrl()
    {
        return urls.project(projectName);
    }
}
