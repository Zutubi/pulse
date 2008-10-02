package com.zutubi.pulse.acceptance.pages.server;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The server info page shows system information for the server and VM.
 */
public class ServerInfoPage extends SeleniumPage
{
    public ServerInfoPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "server.info", "server info");
    }

    public String getUrl()
    {
        return urls.serverInfo();
    }
}
