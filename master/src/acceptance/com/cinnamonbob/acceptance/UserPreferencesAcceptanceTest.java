package com.cinnamonbob.acceptance;

import com.cinnamonbob.acceptance.forms.EditPasswordForm;
import com.cinnamonbob.acceptance.forms.EditUserForm;
import com.cinnamonbob.acceptance.forms.EmailContactForm;
import com.cinnamonbob.acceptance.forms.UserSettingsForm;
import com.cinnamonbob.core.util.RandomUtils;

/**
 *
 *
 */
public class UserPreferencesAcceptanceTest extends BaseAcceptanceTest
{
    private String login;
    private static final String CONTACT_CREATE = "contact.create";
    private static final String CONTACT_CREATE_TYPE = "contact";
    private static final String EMAIL_CREATE = "email.create";
    private static final String EMAIL_CREATE_NAME = "contact.name";
    private static final String EMAIL_CREATE_EMAIL = "contact.email";
    private static final String CREATE_CONTACT_LINK = "create contact";
    //TODO - replace this string with a reference to the properties file.
    private static final String CONTACT_REQUIRED = "you must create a contact point before you can create a subscription";

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

        // navigate to the preferences tab.
        navigateToPreferences();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testUserPreferences()
    {
        // assert tabular data.
        assertTablePresent("user");
        assertTableRowsEqual("user", 1, new String[][]{
                new String[]{"login", login},   // login row
                new String[]{"name", login}     // name row
        });

        assertSettingsTable("welcome", "every 60 seconds");

        assertTablePresent("contacts");
        assertTableRowsEqual("contacts", 1, new String[][]{
                new String[]{"name", "uid", "actions"},
                new String[]{CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK}
        });


        assertTablePresent("subscriptions");
        assertTableRowsEqual("subscriptions", 1, new String[][]{
                new String[]{"project", CONTACT_CREATE_TYPE, "condition", "actions"},
                new String[]{CONTACT_REQUIRED, CONTACT_REQUIRED, CONTACT_REQUIRED, CONTACT_REQUIRED}
        });

        // can not create subscriptions unless there are projects to subscribe to.
        assertLinkNotPresentWithText("create subscription");
    }

    public void testContactFormValidation()
    {
        clickLinkWithText(CREATE_CONTACT_LINK);

        assertFormPresent(CONTACT_CREATE);
        setWorkingForm(CONTACT_CREATE);
        setFormElement(CONTACT_CREATE_TYPE, "email");
        submit("next");

        assertFormPresent(EMAIL_CREATE);
        setWorkingForm(EMAIL_CREATE);
        setFormElement(EMAIL_CREATE_NAME, "");
        setFormElement(EMAIL_CREATE_EMAIL, "email@example.com");
        submit("next");

        assertTextPresent("required");
        assertFormPresent(EMAIL_CREATE);
        assertFormElementEquals(EMAIL_CREATE_NAME, "");
        assertFormElementEquals(EMAIL_CREATE_EMAIL, "email@example.com");

        setWorkingForm(EMAIL_CREATE);
        setFormElement(EMAIL_CREATE_NAME, "example");
        setFormElement(EMAIL_CREATE_EMAIL, "");
        submit("next");

        assertTextPresent("required");
        assertFormPresent(EMAIL_CREATE);
        assertFormElementEquals(EMAIL_CREATE_NAME, "example");
        assertFormElementEquals(EMAIL_CREATE_EMAIL, "");

        setWorkingForm(EMAIL_CREATE);
        setFormElement(EMAIL_CREATE_NAME, "example");
        setFormElement(EMAIL_CREATE_EMAIL, "incorrect email address");
        submit("next");

        assertTextPresent("valid");
        assertFormPresent(EMAIL_CREATE);
        assertFormElementEquals(EMAIL_CREATE_NAME, "example");
        assertFormElementEquals(EMAIL_CREATE_EMAIL, "incorrect email address");

        setWorkingForm(EMAIL_CREATE);
        setFormElement(EMAIL_CREATE_NAME, "example");
        setFormElement(EMAIL_CREATE_EMAIL, "email@example.com");
        submit("cancel");

        // assert that the contact was not created.
        assertTablePresent("contacts");
        assertTextNotPresent("example");
        assertTextNotPresent("email@example.com");
    }

    public void testEditUser()
    {
        // assert tabular data.
        assertTablePresent("user");
        assertTableRowsEqual("user", 1, new String[][]{
                new String[]{"login", login},   // login row
                new String[]{"name", login}     // name row
        });

        assertLinkPresent("user.edit");
        clickLink("user.edit");

        EditUserForm form = new EditUserForm(tester);

        form.assertFormElements(login);
        form.saveFormElements("S. O. MeBody");

        assertTablePresent("user");
        assertTableRowsEqual("user", 1, new String[][]{
                new String[]{"login", login},   // login row
                new String[]{"name", "S. O. MeBody"}     // name row
        });
    }

    public void testCancelEditUser()
    {
        // assert tabular data.
        assertTablePresent("user");
        assertTableRowsEqual("user", 1, new String[][]{
                new String[]{"login", login},   // login row
                new String[]{"name", login}     // name row
        });

        assertLinkPresent("user.edit");
        clickLink("user.edit");

        EditUserForm form = new EditUserForm(tester);

        form.assertFormElements(login);
        form.cancelFormElements("S. O. MeBody");

        // assert tabular data.
        assertTablePresent("user");
        assertTableRowsEqual("user", 1, new String[][]{
                new String[]{"login", login},   // login row
                new String[]{"name", login}     // name row
        });
    }

    public void testEditUserValidation()
    {
        clickLink("user.edit");

        EditUserForm form = new EditUserForm(tester);

        form.assertFormElements(login);
        form.saveFormElements("");

        // assert validation failed.
        form.assertFormElements("");
        assertTextPresent("required");
    }

    public void testEditPassword()
    {
        assertLinkPresent("user.edit");
        clickLink("user.edit");

        EditPasswordForm form = new EditPasswordForm(tester);
        form.assertFormElements("", "", "");
        form.saveFormElements(login, "newPassword", "newPassword");

        // assert that we are back on the preferences page.
        assertTablePresent("user");
        assertLinkPresent("user.edit");

        // now to verify that the password was actually changed.
        login(login, "newPassword");
        assertTextPresent("welcome");
    }

    public void testEditPasswordValidation()
    {
        assertLinkPresent("user.edit");
        clickLink("user.edit");

        EditPasswordForm form = new EditPasswordForm(tester);
        form.assertFormElements("", "", "");

        // check that each field is required.
        form.saveFormElements("a", "a", "");
        assertTextPresent("required");

        form.saveFormElements("b", "", "b");
        assertTextPresent("required");

        form.saveFormElements("", "c", "c");
        assertTextPresent("required");

        // check that the current password is correctly checked.
        form.saveFormElements("incorrect", "a", "a");
        assertTextPresent("does not match");

        // check that the new password and confirm password are correctly checked.
        form.saveFormElements(login, "a", "b");
        assertTextPresent("does not match");

    }

    public void testEditSettings()
    {
        assertAndClick("user.settings");

        UserSettingsForm form = new UserSettingsForm(tester);
        form.assertFormPresent();

        form.assertFormElements("welcome", "true", "60");
        form.saveFormElements("dashboard", "false", "60");

        assertSettingsTable("dashboard", "never");

        assertAndClick("user.settings");
        form.assertFormPresent();
        form.assertFormElements("dashboard", "false", "60");
    }

    public void testEditSettingsCancel()
    {
        assertAndClick("user.settings");

        UserSettingsForm form = new UserSettingsForm(tester);
        form.assertFormPresent();

        form.assertFormElements("welcome", "true", "60");
        form.cancelFormElements("dashboard", "false", null);

        assertSettingsTable("welcome", "every 60 seconds");
    }

    public void testEditSettingsValidation()
    {
        assertAndClick("user.settings");

        UserSettingsForm form = new UserSettingsForm(tester);
        form.assertFormPresent();

        form.assertFormElements("welcome", "true", "60");
        form.saveFormElements("dashboard", "true", "0");
        form.assertFormPresent();
        assertTextPresent("refresh interval must be a positive number");
    }

    private void assertSettingsTable(String defaultAction, String refreshInterval)
    {
        assertTablePresent("settings");
        assertTableRowsEqual("settings", 1, new String[][]{
                new String[]{"default page", defaultAction},
                new String[]{"refresh live content", refreshInterval}
        });

    }

    public void testSubscriptionFormValidation()
    {
        // can not create subscription without project
        // can not create subscription without contacts.
        assertLinkNotPresent("subscription.create");

        assertTablePresent("subscriptions");
        assertTableRowsEqual("subscriptions", 1, new String[][]{
                new String[]{"project", CONTACT_CREATE_TYPE, "condition", "actions"},
                new String[]{CONTACT_REQUIRED, CONTACT_REQUIRED, CONTACT_REQUIRED, CONTACT_REQUIRED}
        });
    }

    public void testCreateContactPoint()
    {
        // test creation of a contact point.
        assertLinkPresentWithText(CREATE_CONTACT_LINK);

        createEmailContactPoint("home", "user@example.com");

        // assert that the contact appears as expected.
        assertTablePresent("contacts");
        assertTableRowsEqual("contacts", 1, new String[][]{
                new String[]{"name", "uid", "actions", "actions"},               // header row
                new String[]{"home", "user@example.com", "edit", "delete"},     // name row
                new String[]{CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK}
        });
    }

    private void createEmailContactPoint(String name, String email)
    {
        clickLinkWithText(CREATE_CONTACT_LINK);
        assertFormPresent(CONTACT_CREATE);

        setWorkingForm(CONTACT_CREATE);
        setFormElement(CONTACT_CREATE_TYPE, "email");
        submit("next");

        assertFormPresent(EMAIL_CREATE);
        setWorkingForm(EMAIL_CREATE);
        setFormElement(EMAIL_CREATE_NAME, name);
        setFormElement(EMAIL_CREATE_EMAIL, email);
        submit("next");
    }

    public void testEditContactPoint()
    {
        // create a contact point.
        assertLinkPresentWithText(CREATE_CONTACT_LINK);
        createEmailContactPoint("home", "user@example.com");

        // edit the contact point.
        assertLinkPresent("edit_home");
        clickLink("edit_home");

        EmailContactForm form = new EmailContactForm(tester);
        form.assertFormElements("home", "user@example.com", "html");
        form.saveFormElements("newHome", "anotherUser@example.com", "plain");

        // ensure that we have correctly changed the email contact.
        assertLinkPresent("edit_newHome");
        assertLinkNotPresent("edit_home");

        // assert that the contact appears as expected.
        assertTablePresent("contacts");
        assertTableRowsEqual("contacts", 1, new String[][]{
                new String[]{"name", "uid", "actions", "actions"},
                new String[]{"newHome", "anotherUser@example.com", "edit", "delete"},
                new String[]{CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK}
        });
    }

    private void navigateToPreferences()
    {
        gotoPage("/");
        clickLinkWithText("dashboard");
        clickLinkWithText("preferences");
    }
}
