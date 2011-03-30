package com.zutubi.pulse.acceptance.pages.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.AbstractHistoryPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The server history page shows completed builds for all projects.
 */
public class ServerHistoryPage extends AbstractHistoryPage
{
    public ServerHistoryPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "server-history", "server-history");
    }

    @Override
    public String getUrl()
    {
        return urls.serverHistory();
    }
}
