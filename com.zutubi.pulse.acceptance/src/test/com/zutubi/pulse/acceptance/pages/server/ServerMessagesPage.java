package com.zutubi.pulse.acceptance.pages.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The server messages page shows recent server log messages.
 */
public class ServerMessagesPage extends SeleniumPage
{
    private static final String ID_MESSAGES_COUNT = "messages.count";

    private int page = 1;

    public ServerMessagesPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "server.messages.0", "server messages");
    }

    public ServerMessagesPage(SeleniumBrowser browser, Urls urls, int page)
    {
        super(browser, urls, "server.messages." + Integer.toString(page - 1), "server messages");
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
        browser.click(getPageId(page));
        return browser.createPage(ServerMessagesPage.class, page);
    }

    public String getMessagesCountText()
    {
        browser.waitForElement(ID_MESSAGES_COUNT);
        return browser.getText(ID_MESSAGES_COUNT);
    }
}
