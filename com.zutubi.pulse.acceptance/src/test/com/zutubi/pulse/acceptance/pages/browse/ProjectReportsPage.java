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

    public ProjectReportsPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        this(browser, urls, projectName, null);
    }

    public ProjectReportsPage(SeleniumBrowser browser, Urls urls, String projectName, String group)
    {
        super(browser, urls, getId(projectName, group), uriComponentEncode(projectName));
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
        browser.click("apply.button");
    }
}