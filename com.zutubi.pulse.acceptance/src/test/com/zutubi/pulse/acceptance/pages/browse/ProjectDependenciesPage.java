package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * The browse project dependencies page.
 */
public class ProjectDependenciesPage extends AbstractLogPage
{
    private String projectName;

    public ProjectDependenciesPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-dependencies-" + WebUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
    }

    public String getUrl()
    {
        return urls.projectDependencies(projectName);
    }

    public boolean isUpstreamPresent(String project, int row, int column)
    {
        return isProjectPresent("upstream", project, row, column);
    }

    public boolean isDownstreamPresent(String project, int row, int column)
    {
        return isProjectPresent("downstream", project, row, column);
    }

    private boolean isProjectPresent(String section, String project, int row, int column)
    {
        return browser.isElementIdPresent(section + "-" + row + "-" + column + "-" + project);
    }
}
