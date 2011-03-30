package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.AbstractHistoryPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * The project history page shows completed builds for a project.
 */
public class ProjectHistoryPage extends AbstractHistoryPage
{
    private String projectName;

    public ProjectHistoryPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-history-" + projectName, "project-history");
        this.projectName = projectName;
    }

    @Override
    public String getUrl()
    {
        return urls.projectHistory(WebUtils.uriComponentEncode(projectName));
    }
}
