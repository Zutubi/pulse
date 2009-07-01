package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * A page that represents the build log page.
 */
public class BuildLogPage extends AbstractLogPage
{
    private String project;
    private String buildNumber;

    public BuildLogPage(SeleniumBrowser browser, Urls urls, String projectName, long buildNumber)
    {
        super(browser, urls, "build-log-" + projectName + "-" + buildNumber);
        this.project = projectName;
        this.buildNumber = String.valueOf(buildNumber);
    }

    public String getUrl()
    {
        return urls.buildLog(project, buildNumber);
    }
}
