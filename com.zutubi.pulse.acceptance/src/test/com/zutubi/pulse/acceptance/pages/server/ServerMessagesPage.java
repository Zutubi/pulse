package com.zutubi.pulse.acceptance.pages.server;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The server messages page shows recent server log messages.
 */
public class ServerMessagesPage extends SeleniumPage
{
    private static final String ID_MESSAGES_COUNT = "messages.count";

    private int page = 1;

    public ServerMessagesPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "server.messages.0", "server messages");
    }

    public ServerMessagesPage(Selenium selenium, Urls urls, int page)
    {
        super(selenium, urls, "server.messages." + Integer.toString(page - 1), "server messages");
        this.page = page;
    }

    public String getUrl()
    {
        if (page == 1)
        {
            return urls.serverMessages();
        }
        else
        {
            return urls.serverMessages() + Integer.toString(page - 1) + "/";
        }
    }

    public String getPageId(int i)
    {
        return "page." + i;
    }

    public int getPageNumber()
    {
        return page;
    }

    public ServerMessagesPage clickPage(int page)
    {
        selenium.click(getPageId(page));
        return new ServerMessagesPage(selenium, urls, page);
    }

    public String getMessagesCountText()
    {
        SeleniumUtils.waitForElementId(selenium, ID_MESSAGES_COUNT);
        return selenium.getText(ID_MESSAGES_COUNT);
    }
}
