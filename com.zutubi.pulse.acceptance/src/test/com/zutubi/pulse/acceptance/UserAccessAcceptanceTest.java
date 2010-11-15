package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.LoginPage;
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

        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.ensureUser(random);
        xmlRpcHelper.logout();
    }

    public void testDefaultUserAccess()
    {
        // read access to the pulse ui
        assertFalse(getBrowser().isLoggedIn());

        assertTrue(getBrowser().login(random, ""));

        assertAccessible(DashboardPage.class);
        assertAccessible(MyPreferencesPage.class);
        assertAccessible(MyBuildsPage.class);

        assertAccessible(BrowsePage.class);
        assertAccessible(ServerActivityPage.class);
        assertAccessible(AgentsPage.class);

        assertAccessible(urls.admin());

        getBrowser().open(urls.admin());
        getBrowser().waitForPageToLoad();
        assertTrue(getBrowser().isElementIdPresent("tab.administration.projects"));
        assertTrue(getBrowser().isElementIdPresent("tab.administration.agents"));
        assertFalse(getBrowser().isElementIdPresent("tab.administration.groups"));
        assertFalse(getBrowser().isElementIdPresent("tab.administration.users"));
        assertFalse(getBrowser().isElementIdPresent("tab.administration.settings"));
    }

    public void testUserXmlRpcAccess() throws Exception
    {
        xmlRpcHelper.login(random, "");
        xmlRpcHelper.getProjectCount();
        xmlRpcHelper.getAgentCount();
        xmlRpcHelper.getAllProjectNames();

        assertXmlRpcDenied("getAllUserLogins");
    }

    private void assertXmlRpcDenied(String function, Object... args) throws Exception
    {
        try
        {
            xmlRpcHelper.call(function, args);
            fail("Expected AccessDeniedException");
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("AccessDeniedException"));
        }
    }

    private void assertAccessible(String url)
    {
        getBrowser().open(url);
        getBrowser().waitForPageToLoad();

        LoginPage loginPage = getBrowser().createPage(LoginPage.class);
        assertFalse(loginPage.isPresent());
    }

    private <T extends SeleniumPage> void  assertAccessible(Class<T> page, Object... args)
    {
        SeleniumPage p = getBrowser().openAndWaitFor(page, args);
        assertTrue(p.isPresent());
    }


}
