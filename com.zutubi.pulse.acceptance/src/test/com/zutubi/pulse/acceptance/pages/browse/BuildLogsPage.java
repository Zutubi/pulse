package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * A page that represents the build logs tab.  The first stage log is shown by
 * default.
 */
public class BuildLogsPage extends StageLogPage
{
    public BuildLogsPage(SeleniumBrowser browser, Urls urls, String projectName, long buildNumber, String stageName)
    {
        super(browser, urls, projectName, buildNumber, stageName);
    }

    public String getUrl()
    {
        return urls.buildLogs(WebUtils.uriComponentEncode(project), buildNumber);
    }
}