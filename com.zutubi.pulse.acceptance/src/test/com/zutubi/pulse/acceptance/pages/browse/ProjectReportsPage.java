package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.Urls;
import static com.zutubi.util.StringUtils.uriComponentEncode;

/**
 * The project reports page shows trend reports for a project.
 */
public class ProjectReportsPage extends ResponsibilityPage
{
    private String projectName;
    private String group;

    public ProjectReportsPage(Selenium selenium, Urls urls, String projectName, String group)
    {
        super(selenium, urls, "project-reports-" + projectName + "-" + group, uriComponentEncode(projectName));
        this.projectName = projectName;
        this.group = group;
    }

    public String getUrl()
    {
        return urls.projectReports(projectName, group);
    }
}