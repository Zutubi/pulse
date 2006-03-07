package com.cinnamonbob.acceptance;

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
    private static final String EMAIL_CREATE_NAME = "contact.name";
    private static final String EMAIL_CREATE = "email.create";
    private static final String EMAIL_CREATE_EMAIL = "contact.email";
    private static final String CREATE_CONTACT_LINK = "create contact";

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

        assertTablePresent("contacts");
        assertTableRowsEqual("contacts", 1, new String[][]{
                new String[]{"name", "uid", "actions"},
                new String[]{CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK}
        });

        // test creation of a contact point.
        assertLinkPresentWithText(CREATE_CONTACT_LINK);

        clickLinkWithText(CREATE_CONTACT_LINK);
        assertFormPresent(CONTACT_CREATE);

        setWorkingForm(CONTACT_CREATE);
        setFormElement(CONTACT_CREATE_TYPE, "email");
        submit("next");

        assertFormPresent(EMAIL_CREATE);
        setWorkingForm(EMAIL_CREATE);
        setFormElement(EMAIL_CREATE_NAME, "home");
        setFormElement(EMAIL_CREATE_EMAIL, "user@example.com");
        submit("next");

        // assert that the contact appears as expected.
        assertTablePresent("contacts");
        assertTableRowsEqual("contacts", 1, new String[][]{
                new String[]{"name", "uid", "actions", "actions"},               // header row
                new String[]{"home", "user@example.com", "edit", "delete"},     // name row
                new String[]{CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK}
        });


        assertTablePresent("subscriptions");
        assertTableRowsEqual("subscriptions", 1, new String[][]{
                new String[]{"project", CONTACT_CREATE_TYPE, "condition", "actions"},
                new String[]{"create subscription", "create subscription", "create subscription", "create subscription"}
        });


        assertLinkPresentWithText("create subscription");

        // can not create subscriptions unless there are projects to subscribe to.
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

        assertLinkPresentWithText("edit");
        clickLinkWithText("edit");

        assertFormPresent("user.edit");
        setWorkingForm("user.edit");
        assertFormElementEquals("user.name", login);

        setFormElement("user.name", "S. O. MeBody");
        submit("save");

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

        assertLinkPresentWithText("edit");
        clickLinkWithText("edit");

        assertFormPresent("user.edit");
        setWorkingForm("user.edit");
        setFormElement("user.name", "S. O. MeBody");
        submit("cancel");

        // assert tabular data.
        assertTablePresent("user");
        assertTableRowsEqual("user", 1, new String[][]{
                new String[]{"login", login},   // login row
                new String[]{"name", login}     // name row
        });

    }

    public void testEditUserValidation()
    {
        clickLinkWithText("edit");

        assertFormPresent("user.edit");
        setWorkingForm("user.edit");
        assertFormElementEquals("user.name", login);

        setFormElement("user.name", "");
        submit("save");

        // assert validation failed.
        assertTextPresent("required");
        assertFormPresent("user.edit");
        assertFormElementEquals("user.name", "");
    }

    public void testSubscriptionFormValidation()
    {
        // can not create subscription without project
        // can not create subscription without contacts.
    }

    private void navigateToPreferences()
    {
        gotoPage("/");
        clickLinkWithText("dashboard");
        clickLinkWithText("preferences");
    }
}
