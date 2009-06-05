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
        browser.openAndWaitFor(ServerMessagesPage.class);
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
        ServerMessagesPage page = browser.openAndWaitFor(ServerMessagesPage.class);

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
        browser.openAndWaitFor(ServerInfoPage.class);
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
                assertFalse(browser.isElementPresent(pageId));
            }
            else
            {
                assertTrue(browser.isElementPresent(pageId));
            }
        }

        if(page.getPageNumber() == 1)
        {
            assertFalse(browser.isElementPresent("page.latest"));
            assertFalse(browser.isElementPresent("page.previous"));
        }
        else
        {
            assertTrue(browser.isElementPresent("page.latest"));
            assertTrue(browser.isElementPresent("page.previous"));
        }

        if(page.getPageNumber() == pageCount)
        {
            assertFalse(browser.isElementPresent("page.oldest"));
            assertFalse(browser.isElementPresent("page.next"));
        }
        else
        {
            assertTrue(browser.isElementPresent("page.oldest"));
            assertTrue(browser.isElementPresent("page.next"));
        }
    }
}
