package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * A page that represents a personal build's logs tab.  The build log is shown by default.
 */
public class PersonalBuildLogPage extends BuildLogPage
{
    public PersonalBuildLogPage(SeleniumBrowser browser, Urls urls, String projectName, long buildNumber)
    {
        super(browser, urls, projectName, buildNumber);
    }

    @Override
    public String getUrl()
    {
        return urls.myBuildLog(buildNumber);
    }
}
