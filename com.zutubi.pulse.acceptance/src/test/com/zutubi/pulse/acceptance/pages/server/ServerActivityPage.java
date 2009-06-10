package com.zutubi.pulse.acceptance.pages.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The server activity page shows the server queues and active builds.
 */
public class ServerActivityPage extends SeleniumPage
{
    public ServerActivityPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "server.activity", "server activity");
    }

    public String getUrl()
    {
        return urls.serverActivity();
    }
}
