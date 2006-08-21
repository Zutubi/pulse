package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.EditPasswordForm;
import com.zutubi.pulse.acceptance.forms.CreateUserForm;
import com.zutubi.pulse.util.RandomUtils;

/**
 * <class-comment/>
 */
public class UserAdministrationAcceptanceTest extends BaseAcceptanceTestCase
{

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
        loginAsAdmin();

        // navigate to user admin tab.
        navigateToUserAdministration();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testCreateStandardUser()
    {
        // create random login name.
        String login = RandomUtils.randomString(10);

        assertUserNotExists(login);

        submitCreateUserForm(login, login, login, login);

        // assert user does exist.
        assertUserExists(login);

        // assert form is reset.
        assertFormReset();

        // check that we can login with this new user.
        clickLinkWithText("logout");

        login(login, login);

        // if login was successful, should see the welcome page and a logout link.
        assertTextPresent(":: welcome ::");
        assertLinkPresentWithText("logout");

        // ensure that this user does not have admin permissions.
        assertLinkNotPresentWithText("administration");
    }

    public void testCreateUserValidation()
    {
        // create random login name.
        String login = RandomUtils.randomString(10);

        // check validation - login is required.
        CreateUserForm form = new CreateUserForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", login, Boolean.toString(false), login, login);

        // should get an error message.
        assertTextPresent("required");
        assertLinkNotPresentWithText(login);

        form.assertFormElements("", login, Boolean.toString(false), "", "");


        // check validation - password and confirmation mismatch
        form.saveFormElements(login, login, Boolean.toString(false), login, "something not very random");

        assertTextPresent("does not match");
        assertLinkNotPresentWithText(login);
        form.assertFormElements(login, login, Boolean.toString(false), "", "");
    }

    public void testDeleteUser()
    {
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

    public void testViewUser()
    {
        // create user.
        String login = RandomUtils.randomString(10);
        submitCreateUserForm(login, login, login, login);

        // view user
        assertLinkPresentWithText(login);
        clickLinkWithText(login);

        // assert tabular data.
        assertTablePresent("user");
        assertTableRowsEqual("user", 1, new String[][]{
                new String[]{"login", login},   // login row
                new String[]{"name", login}     // name row
        });

        // switch to user, create contacts and subscriptions, assert they appear.
    }

    public void testChangeUserPassword()
    {
        // create a user.
        String login = RandomUtils.randomString(7);
        submitCreateUserForm(login, login, login, login);

        assertLinkPresent("edit_" + login);
        clickLink("edit_" + login);

        EditPasswordForm form = new EditPasswordForm(tester);
        form.assertFormElements("", "");
        form.saveFormElements("newPassword", "newPassword");

        // assert that we are back on the manage users

        // now to verify that the password was actually changed.
        login(login, "newPassword");
        assertTextPresent("welcome");
    }

    public void testChangeUserPasswordValidation()
    {
        // create a user.
        String login = RandomUtils.randomString(7);
        submitCreateUserForm(login, login, login, login);

        assertLinkPresent("edit_" + login);
        clickLink("edit_" + login);

        EditPasswordForm form = new EditPasswordForm(tester);
        form.assertFormElements("", "");

        // check that each field is required.
        form.saveFormElements("a", "");
        assertTextPresent("required");

        form.saveFormElements("", "b");
        assertTextPresent("required");

        // check that the new password and confirm password are correctly checked.
        form.saveFormElements("a", "b");
        assertTextPresent("does not match");
    }

    private void assertFormReset()
    {
        CreateUserForm form = new CreateUserForm(tester);
        form.assertFormReset();
    }

    private void assertUserExists(String login)
    {
        // PROBLEM: this will only work while there is no pagination. we need another way to
        // lookup a user, a more direct method.
        assertTextPresent(login);
        assertLinkPresentWithText(login);
    }

    private void assertUserNotExists(String login)
    {
        // PROBLEM: this will only work while there is no pagination. we need another way to
        // lookup a user, a more direct method.
        assertTextNotPresent(login);
        assertLinkNotPresentWithText(login);
    }
}
