package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The my builds page shows a user's personal builds.
 */
public class MyBuildsPage extends SeleniumPage
{
    public MyBuildsPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "personal-builds", "my");
    }

    public String getUrl()
    {
        return urls.dashboardMyBuilds();
    }

    public static String getBuildNumberId(long buildNumber)
    {
        return getIdPrefix(buildNumber) + ".id";
    }

    public static String getBuildStatusId(long buildNumber)
    {
        return getIdPrefix(buildNumber) + ".status";
    }

    private static String getIdPrefix(long buildNumber)
    {
        return "personal.build." + buildNumber;
    }
}
