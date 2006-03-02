package com.cinnamonbob.acceptance;

import com.cinnamonbob.core.util.RandomUtils;

/**
 * <class-comment/>
 */
public class UserAdministrationAcceptanceTest extends BaseAcceptanceTest
{
    private static final String FE_LOGIN = "user.login";
    private static final String FE_NAME = "user.name";
    private static final String FE_PASSWORD = "user.password";
    private static final String FE_CONFIRM = "confirm";

    public UserAdministrationAcceptanceTest()
    {
    }

    public UserAdministrationAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        login("admin", "admin");
    }

    protected void tearDown() throws Exception
    {

        super.tearDown();
    }

    public void testCreateUser()
    {
        // navigate to user admin tab.
        navigateToUserAdministration();

        // create random login name.
        String login = RandomUtils.randomString(10);

        // PROBLEM: this will only work while there is no pagination. we need another way to
        // lookup a user, a more direct method.
        assertTextNotPresent(login);
        assertLinkNotPresentWithText(login);

        submitCreateUserForm(login, login, login, login);

        // assert user does exist.
        assertTextPresent(login);
        assertLinkPresentWithText(login);

        // assert form is reset.
        assertFormElementEmpty(FE_LOGIN);
        assertFormElementEmpty(FE_NAME);
        assertFormElementEmpty(FE_PASSWORD);
        assertFormElementEmpty(FE_CONFIRM);

        // check that we can login with this new user.
        clickLinkWithText("logout");

        login(login, login);

        // if login was successful, should see the welcome page and a logout link.
        assertTextPresent(":: welcome ::");
        assertLinkPresentWithText("logout");
    }

    public void testCreateUserValidation()
    {
        // navigate to user admin tab.
        navigateToUserAdministration();

        // create random login name.
        String login = RandomUtils.randomString(10);

        // check validation - login is required.
        submitCreateUserForm("", login, login, login);

        // should get an error message.
        assertTextPresent("required");
        assertLinkNotPresentWithText(login);
        assertFormElementEmpty(FE_LOGIN);
        assertFormElementEquals(FE_NAME, login);

        // check validation - password is required.
        submitCreateUserForm(login, login, "", "");

        assertTextPresent("required");
        assertLinkNotPresentWithText(login);
        assertFormElementEquals(FE_LOGIN, login);
        assertFormElementEquals(FE_NAME, login);

        // check validation - password and confirmation mismatch
        submitCreateUserForm(login, login, login, "something not very random");

        assertTextPresent("does not match");
        assertLinkNotPresentWithText(login);
        assertFormElementEquals(FE_LOGIN, login);
        assertFormElementEquals(FE_NAME, login);
    }

    public void testDeleteUser()
    {
        // navigate to the user admin pages.
        navigateToUserAdministration();

        // create a user to delete - assume that user creation is successful?
        String login = RandomUtils.randomString(10);
        submitCreateUserForm(login, login, login, login);
        // check that it worked.
        assertLinkPresentWithText(login);
        assertLinkPresent("delete_" + login);

        clickLink("delete_" + login);

        // hmm, now there should be a confirm delete dialog box that appears,
        // but for some reason, i does not appear to exist in the list of open windows.
        // odd...

        assertLinkNotPresentWithText(login);
    }

    private void submitCreateUserForm(String login, String name, String password, String confirm)
    {
        setWorkingForm("user.create");
        setFormElement(FE_LOGIN, login);
        setFormElement(FE_NAME, name);
        setFormElement(FE_PASSWORD, password);
        setFormElement(FE_CONFIRM, confirm);
        submit("save");
    }

    private void navigateToUserAdministration()
    {
        gotoPage("/");
        clickLinkWithText("administration");
        clickLinkWithText("users");
    }
}
