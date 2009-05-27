package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.server.ServerInfoPage;
import com.zutubi.pulse.acceptance.pages.server.ServerMessagesPage;

/**
 * Acceptance tests for the server section of the reporting UI.
 */
public class ServerSectionAcceptanceTest extends SeleniumTestBase
{
    public void testServerMessages() throws Exception
    {
        loginAsAdmin();
        ServerMessagesPage page = new ServerMessagesPage(selenium, urls);
        page.goTo();

        assertTextPresent("messages found");
    }

    public void testServerMessagesPaging() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            for (int i = 0; i < 100; i++)
            {
                xmlRpcHelper.logError("Test error message " + i);
            }
        }
        finally
        {
            xmlRpcHelper.logout();
        }

        loginAsAdmin();
        ServerMessagesPage page = new ServerMessagesPage(selenium, urls);
        page.goTo();

        assertEquals("100 messages found", page.getMessagesCountText());
        assertPagingLinks(page, 10);
        page = page.clickPage(5);
        page.waitFor();
        assertPagingLinks(page, 10);
        assertTextPresent("Test error message 59");
    }

    public void testServerInfo() throws Exception
    {
        loginAsAdmin();
        ServerInfoPage page = new ServerInfoPage(selenium, urls);
        page.goTo();

        assertTextPresent("system information");
        assertTextPresent("java vm");
        assertTextPresent("version information");
        assertTextPresent("version number");
        assertTextPresent("pulse configuration");
        assertTextPresent("data directory");
        assertTextPresent("all system properties");
        assertTextPresent("path.separator");
    }

    private void assertPagingLinks(ServerMessagesPage page, int pageCount)
    {
        for(int i = 1; i <= pageCount; i++)
        {
            String pageId = page.getPageId(i);
            if(i == page.getPageNumber())
            {
                assertFalse(selenium.isElementPresent(pageId));
            }
            else
            {
                assertTrue(selenium.isElementPresent(pageId));
            }
        }

        if(page.getPageNumber() == 1)
        {
            assertFalse(selenium.isElementPresent("page.latest"));
            assertFalse(selenium.isElementPresent("page.previous"));
        }
        else
        {
            assertTrue(selenium.isElementPresent("page.latest"));
            assertTrue(selenium.isElementPresent("page.previous"));
        }

        if(page.getPageNumber() == pageCount)
        {
            assertFalse(selenium.isElementPresent("page.oldest"));
            assertFalse(selenium.isElementPresent("page.next"));
        }
        else
        {
            assertTrue(selenium.isElementPresent("page.oldest"));
            assertTrue(selenium.isElementPresent("page.next"));
        }
    }
}
