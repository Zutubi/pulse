package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.components.Pager;
import com.zutubi.pulse.acceptance.pages.agents.AgentMessagesPage;
import com.zutubi.pulse.acceptance.pages.server.ServerMessagesPage;
import com.zutubi.pulse.master.agent.AgentManager;

/**
 * Acceptance tests for the server and agent messages tabs.
 */
public class ServerMessagesAcceptanceTest extends AcceptanceTestBase
{
    public void testServerMessages() throws Exception
    {
        rpcClient.loginAsAdmin();
        try
        {
            rpcClient.TestApi.logWarning("Testing one");
            rpcClient.TestApi.logWarning("Testing two");
        }
        finally
        {
            rpcClient.logout();
        }

        getBrowser().loginAsAdmin();
        getBrowser().openAndWaitFor(ServerMessagesPage.class, 1);
        getBrowser().waitForTextPresent("messages found");
    }

    public void testServerMessagesPaging() throws Exception
    {
        ServerMessagesPage page = getBrowser().createPage(ServerMessagesPage.class, 1);
        pagingTestHelper(page);
    }

    public void testAgentMessagesAgentOffline() throws Exception
    {
        rpcClient.loginAsAdmin();
        String agentPath = rpcClient.RemoteApi.insertSimpleAgent(random, "localhost", 555666777);
        try
        {
            getBrowser().loginAsAdmin();
            getBrowser().openAndWaitFor(AgentMessagesPage.class, random, 1);
            getBrowser().waitForTextPresent("Agent is not online");
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(agentPath);
            rpcClient.logout();
        }
    }

    public void testAgentMessagesPaging() throws Exception
    {
        ServerMessagesPage page = getBrowser().createPage(AgentMessagesPage.class, AgentManager.MASTER_AGENT_NAME, 1);
        pagingTestHelper(page);
    }

    private void pagingTestHelper(ServerMessagesPage page) throws Exception
    {
        rpcClient.loginAsAdmin();
        try
        {
            for (int i = 0; i < 100; i++)
            {
                rpcClient.TestApi.logError("Test error message " + i);
            }
        }
        finally
        {
            rpcClient.logout();
        }

        getBrowser().loginAsAdmin();
        page.openAndWaitFor();
        assertEquals(10, page.getEntries().getEntryCount());
        assertEquals("Test error message 99", page.getEntries().getEntryMessage(0));

        Pager pager = page.getPager();
        assertEquals(100, pager.getTotalItems());
        assertEquals(0, pager.getCurrentPage());
        assertFalse(pager.hasFirstLink());
        assertFalse(pager.hasPreviousLink());
        assertTrue(pager.hasNextLink());
        assertTrue(pager.hasLastLink());

        pager.clickNext();
        page = page.createNextPage();
        page.waitFor();
        assertEquals("Test error message 89", page.getEntries().getEntryMessage(0));

        pager = page.getPager();
        assertEquals(100, pager.getTotalItems());
        assertEquals(1, pager.getCurrentPage());
        assertTrue(pager.hasFirstLink());
        assertTrue(pager.hasPreviousLink());
        assertTrue(pager.hasNextLink());
        assertTrue(pager.hasLastLink());
    }
}
