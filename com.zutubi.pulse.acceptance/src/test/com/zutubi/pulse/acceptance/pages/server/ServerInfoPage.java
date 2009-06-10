package com.zutubi.pulse.acceptance.pages.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The server info page shows system information for the server and VM.
 */
public class ServerInfoPage extends SeleniumPage
{
    public ServerInfoPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "server.info", "server info");
    }

    public String getUrl()
    {
        return urls.serverInfo();
    }
}
