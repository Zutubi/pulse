package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The project reports page shows trend reports for a project.
 */
public class ProjectReportsPage extends ResponsibilityPage
{
    private String projectName;

    public ProjectReportsPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-reports-" + projectName, projectName);
        this.projectName = projectName;
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel.loaded");
    }

    public String getUrl()
    {
        return urls.projectReports(projectName);
    }

    public void clickApply()
    {
        browser.click("reports-apply");
    }
}