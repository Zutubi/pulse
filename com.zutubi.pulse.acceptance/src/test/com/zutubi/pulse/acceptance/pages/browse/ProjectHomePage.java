package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The project home page is a summary of the state and recent activity for a
 * project.
 */
public class ProjectHomePage extends ResponsibilityPage
{
    private String projectName;

    public ProjectHomePage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-home-" + StringUtils.uriComponentEncode(projectName), StringUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
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
