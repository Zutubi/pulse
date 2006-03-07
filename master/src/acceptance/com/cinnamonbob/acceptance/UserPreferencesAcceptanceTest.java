package com.cinnamonbob.acceptance;

import com.cinnamonbob.core.util.RandomUtils;

/**
 *
 *
 */
public class UserPreferencesAcceptanceTest extends BaseAcceptanceTest
{
    private String login;

    public UserPreferencesAcceptanceTest()
    {
    }

    public UserPreferencesAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // create new user..
        login("admin", "admin");
        navigateToUserAdministration();
        login = RandomUtils.randomString(7);
        submitCreateUserForm(login, login, login, login, false);

        login(login, login);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testUserPreferences()
    {
        // navigate to the preferences tab.
        gotoPage("/");
        clickLinkWithText("dashboard");
        clickLinkWithText("preferences");

        // assert tabular data.
        assertTablePresent("user");
        assertTableRowsEqual("user", 1, new String[][]{
                new String[]{"login", login},   // login row
                new String[]{"name", login}     // name row
        });

        assertLinkPresentWithText("create contact");
        assertLinkPresentWithText("create subscription");
    }
}
