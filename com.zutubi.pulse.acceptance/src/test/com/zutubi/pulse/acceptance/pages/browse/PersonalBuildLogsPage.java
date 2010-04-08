package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * A page that represents a personal build's logs tab.  The first stage log is shown by
 * default.
 */
public class PersonalBuildLogsPage extends BuildLogsPage
{
    public PersonalBuildLogsPage(SeleniumBrowser browser, Urls urls, String projectName, long buildNumber, String stageName)
    {
        super(browser, urls, projectName, buildNumber, stageName);
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildLogs(buildNumber);
    }
}