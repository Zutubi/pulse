package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import static com.zutubi.util.StringUtils.uriComponentEncode;

/**
 * The project reports page shows trend reports for a project.
 */
public class ProjectReportsPage extends ResponsibilityPage
{
    private String projectName;
    private String group;

    public ProjectReportsPage(SeleniumBrowser browser, Urls urls, String projectName, String group)
    {
        super(browser, urls, "project-reports-" + projectName + "-" + group, uriComponentEncode(projectName));
        this.projectName = projectName;
        this.group = group;
    }

    public String getUrl()
    {
        return urls.projectReports(projectName, group);
    }
}