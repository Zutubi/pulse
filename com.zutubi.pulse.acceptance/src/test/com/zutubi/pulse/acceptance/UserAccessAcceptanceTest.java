package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.pages.agents.AgentsPage;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.acceptance.pages.dashboard.MyBuildsPage;
import com.zutubi.pulse.acceptance.pages.dashboard.MyPreferencesPage;
import com.zutubi.pulse.acceptance.pages.server.ServerActivityPage;

public class UserAccessAcceptanceTest extends AcceptanceTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.ensureUser(random);
        rpcClient.logout();
    }

    public void testDefaultUserAccess()
    {
        // read access to the pulse ui
        assertFalse(getBrowser().isLoggedIn());

        assertTrue(getBrowser().login(random, ""));

        checkAccessible(DashboardPage.class);
        checkAccessible(MyPreferencesPage.class);
        checkAccessible(MyBuildsPage.class);

        checkAccessible(BrowsePage.class);
        checkAccessible(ServerActivityPage.class);
        checkAccessible(AgentsPage.class);

        getBrowser().open(urls.admin());
        getBrowser().waitForElement("tab.administration.projects");
        getBrowser().waitForElement("tab.administration.agents");
        assertFalse(getBrowser().isElementIdPresent("tab.administration.groups"));
        assertFalse(getBrowser().isElementIdPresent("tab.administration.users"));
        assertFalse(getBrowser().isElementIdPresent("tab.administration.settings"));
    }

    public void testUserXmlRpcAccess() throws Exception
    {
        rpcClient.login(random, "");
        rpcClient.RemoteApi.getProjectCount();
        rpcClient.RemoteApi.getAgentCount();
        rpcClient.RemoteApi.getAllProjectNames();

        assertXmlRpcDenied("getAllUserLogins");
    }

    private void assertXmlRpcDenied(String function, Object... args) throws Exception
    {
        try
        {
            rpcClient.RemoteApi.call(function, args);
            fail("Expected AccessDeniedException");
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("AccessDeniedException"));
        }
    }

    private <T extends SeleniumPage> void checkAccessible(Class<T> page, Object... args)
    {
        getBrowser().openAndWaitFor(page, args);
    }
}
