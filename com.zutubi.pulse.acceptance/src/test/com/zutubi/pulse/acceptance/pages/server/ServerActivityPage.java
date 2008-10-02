package com.zutubi.pulse.acceptance.pages.server;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The server activity page shows the server queues and active builds.
 */
public class ServerActivityPage extends SeleniumPage
{
    public ServerActivityPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "server.activity", "server activity");
    }

    public String getUrl()
    {
        return urls.serverActivity();
    }
}
