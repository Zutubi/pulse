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

    public void assertPagingLinks(int pageCount)
    {
        for(int i = 1; i <= pageCount; i++)
        {
            String pageId = getPageId(i);
            if(i == page)
            {
                SeleniumUtils.assertElementNotPresent(selenium, pageId);
            }
            else
            {
                SeleniumUtils.assertElementPresent(selenium, pageId);
            }

        }

        if(page == 1)
        {
            SeleniumUtils.assertElementNotPresent(selenium, "page.latest");
            SeleniumUtils.assertElementNotPresent(selenium, "page.previous");
        }
        else
        {
            SeleniumUtils.assertElementPresent(selenium, "page.latest");
            SeleniumUtils.assertElementPresent(selenium, "page.previous");
        }

        if(page == pageCount)
        {
            SeleniumUtils.assertElementNotPresent(selenium, "page.oldest");
            SeleniumUtils.assertElementNotPresent(selenium, "page.next");
        }
        else
        {
            SeleniumUtils.assertElementPresent(selenium, "page.oldest");
            SeleniumUtils.assertElementPresent(selenium, "page.next");
        }
    }

    private String getPageId(int i)
    {
        return "page." + i;
    }

    public ServerMessagesPage clickPage(int page)
    {
        selenium.click(getPageId(page));
        return new ServerMessagesPage(selenium, urls, page);
    }
}
