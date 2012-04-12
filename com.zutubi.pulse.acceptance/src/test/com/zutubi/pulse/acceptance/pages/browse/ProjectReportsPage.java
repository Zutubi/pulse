package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import org.openqa.selenium.By;

/**
 * The project reports page shows trend reports for a project.
 */
public class ProjectReportsPage extends ResponsibilityPage
{
    private String projectName;
    private String group;

    public ProjectReportsPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        this(browser, urls, projectName, null);
    }

    public ProjectReportsPage(SeleniumBrowser browser, Urls urls, String projectName, String group)
    {
        super(browser, urls, getId(projectName, group), projectName);
        this.projectName = projectName;
        this.group = group;
    }

    private static String getId(String projectName, String group)
    {
        String id = "project-reports-" + projectName;
        if (group != null)
        {
            id += "-" + group;
        }

        return id;
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel.loaded");
    }

    public String getUrl()
    {
        if (group == null)
        {
            return urls.projectReports(projectName);
        }
        else
        {
            return urls.projectReports(projectName, group);
        }
    }

    public void clickApply()
    {
        browser.click(By.id("reports-apply"));
    }
}