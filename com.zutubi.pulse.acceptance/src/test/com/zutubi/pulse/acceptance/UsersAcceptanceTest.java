package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.UsersPage;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;

public class UsersAcceptanceTest extends SeleniumTestBase
{
    private static final String STATE_LAST_ACCESS = "lastAccess";
    private static final String ACCESS_NEVER = "never";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.deleteAllConfigs(PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testActiveUsers() throws Exception
    {
        xmlRpcHelper.insertTrivialUser(random);

        loginAsAdmin();
        UsersPage usersPage = browser.openAndWaitFor(UsersPage.class);

        assertTrue(usersPage.isActiveCountPresent());
        assertEquals("1 of 2 users have been active in the last 10 minutes", usersPage.getActiveCount());

        logout();
        login(random, "");
        logout();

        loginAsAdmin();
        usersPage.openAndWaitFor();
        assertEquals("2 of 2 users have been active in the last 10 minutes", usersPage.getActiveCount());
    }

    public void testLastAccessTime() throws Exception
    {
        String userPath = xmlRpcHelper.insertTrivialUser(random);

        loginAsAdmin();
        CompositePage userPage = browser.openAndWaitFor(CompositePage.class, userPath);

        assertTrue(userPage.isStateFieldPresent(STATE_LAST_ACCESS));
        assertEquals(ACCESS_NEVER, userPage.getStateField(STATE_LAST_ACCESS));

        logout();
        login(random, "");
        logout();

        loginAsAdmin();
        userPage.openAndWaitFor();
        assertFalse(userPage.getStateField(STATE_LAST_ACCESS).equals(ACCESS_NEVER));
    }
}
