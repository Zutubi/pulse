package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.pulse.project.BuildSummaryTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.master.webwork.Urls;

import java.util.List;

/**
 * The my builds page shows a user's personal builds.
 */
public class MyBuildsPage extends SeleniumPage
{
    private BuildSummaryTable buildsTable;
    
    public MyBuildsPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "my-builds", "my");
        buildsTable = new BuildSummaryTable(browser, "my-builds-builds");
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel.initialised");
    }

    public String getUrl()
    {
        return urls.dashboardMyBuilds();
    }

    public List<BuildInfo> getBuilds()
    {
        return buildsTable.getBuilds();
    }
}
