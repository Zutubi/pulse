package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.*;
import com.zutubi.pulse.util.RandomUtils;
import net.sourceforge.jwebunit.ExpectedRow;
import net.sourceforge.jwebunit.ExpectedTable;

/**
 *
 *
 */
public class UserPreferencesAcceptanceTest extends BaseAcceptanceTestCase
{
    private String login;

    private static final String CREATE_CONTACT_LINK = "create contact";
    //TODO - replace this string with a reference to the properties file.
    private static final String CONTACT_REQUIRED = "you must create a contact point before you can create a subscription";
    private static final String JABBER_CONTACT = "myjabber";

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
        loginAsAdmin();
        navigateToUserAdministration();
        login = RandomUtils.randomString(7);
        submitCreateUserForm(login, login, login, login);

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

        assertAliasesTable();

        assertSettingsTable("welcome", "5", "every 60 seconds", "30", "every 60 seconds");

        assertTablePresent("contacts");
        assertTableRowsEqual("contacts", 1, new String[][]{
                new String[]{"name", "uid", "actions"},
                new String[]{CREATE_CONTACT_LINK, CREATE_CONTACT_LINK, CREATE_CONTACT_LINK}
        });


        assertTablePresent("subscriptions");
        assertTableRowsEqual("subscriptions", 1, new String[][]{
                new String[]{"contact", "subscribed to", "actions"},
                new String[]{CONTACT_REQUIRED, CONTACT_REQUIRED, CONTACT_REQUIRED}
        });

        // can not create subscriptions unless there are projects to subscribe to.
        assertLinkNotPresentWithText("create subscription");
    }

    //---( test the email contact point )---
    public EmailContactForm emailSetup()
    {
        assertAndClick("contact.create");

        CreateContactForm contactForm = new CreateContactForm(tester);
        contactForm.assertFormPresent();
        assertOptionsEqual("contact", new String[]{ "email", "jabber" });
        contactForm.nextFormElements("email");

        EmailContactForm emailForm = new EmailContactForm(tester, true);
        emailForm.assertFormPresent();
        return emailForm;
    }

    public void testAddEmailContactValidation()
    {
        EmailContactForm emailForm = emailSetup();

        // name is required.
        emailForm.finishFormElements("", "email@example.com");
        emailForm.assertFormPresent();
        emailForm.assertFormElements("", "email@example.com");
        assertTextPresent("required");

        // email is required
        emailForm.finishFormElements("example", "");
        emailForm.assertFormPresent();
        emailForm.assertFormElements("example", "");
        assertTextPresent("required");

        // email must be valid
        emailForm.finishFormElements("example", "incorrect email address");
        emailForm.assertFormPresent();
        emailForm.assertFormElements("example", "incorrect email address");
        assertTextPresent("valid");

        // no need to check the radio box here.
    }

    public void testCancelEmailContact()
    {
        EmailContactForm emailForm = emailSetup();

        emailForm.cancelFormElements("example", "email@example.com");
        emailForm.assertFormNotPresent();

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
        form.assertFormNotPresent();

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

        // the only requirement is that the fields match. None of the fields are required.
        form.saveFormElements("a", "a", "");
        assertTextPresent("does not match");

        form.saveFormElements("b", "", "b");
        assertTextPresent("does not match");

        form.saveFormElements("", "c", "c");
        assertTextPresent("does not match");

        // check that the current password is correctly checked.
        form.saveFormElements("incorrect", "a", "a");
        assertTextPresent("does not match");

        // check that the new password and confirm password are correctly checked.
        form.saveFormElements(login, "a", "b");
        assertTextPresent("does not match");
    }

    public void testSetAndEditABlankPassword()
    {
        // We have no requirements for passwords, so lets ensure that blank passwords work.
        assertLinkPresent("user.edit");
        clickLink("user.edit");

        // change the password to blank.
        EditPasswordForm form = new EditPasswordForm(tester);
        form.assertFormElements("", "", "");
        form.saveFormElements(login, "", "");
        form.assertFormNotPresent();

        // assert that we are back on the preferences page.
        assertTablePresent("user");
        assertLinkPresent("user.edit");

        // now to verify that the password was actually changed.
        login(login, "");
        assertTextPresent("welcome");

        // navigate tot he edit password form.
        navigateToPreferences();
        assertLinkPresent("user.edit");
        clickLink("user.edit");

        // change the password fro blank to something.
        form = new EditPasswordForm(tester);
        form.assertFormElements("", "", "");
        form.saveFormElements("", "something", "something");
        form.assertFormNotPresent();
    }

    public void testAddAlias()
    {
        assertAndClick("alias.add");

        AddAliasForm form = new AddAliasForm(tester);
        form.assertFormPresent();

        form.saveFormElements("alias1");

        assertAliasesTable("alias1");
    }

    public void testAddAliasValidation()
    {
        testAddAlias();

        assertAndClick("alias.add");

        AddAliasForm form = new AddAliasForm(tester);
        form.assertFormPresent();

        form.saveFormElements("");
        form.assertFormPresent();
        assertTextPresent("alias is required");

        form.saveFormElements("alias1");
        form.assertFormPresent();
        assertTextPresent("you have already configured an alias with the same value");
    }

    public void testAddAliasCancel()
    {
        assertAndClick("alias.add");

        AddAliasForm form = new AddAliasForm(tester);
        form.assertFormPresent();
        form.cancelFormElements("aliasCancelled");
        assertAliasesTable();
    }

    public void testDeleteAlias()
    {
        testAddAlias();
        assertAndClick("deleteAlias1");
        assertAliasesTable();
    }

    public void testEditSettings()
    {
        assertAndClick("user.settings");

        UserSettingsForm form = new UserSettingsForm(tester);
        form.assertFormPresent();
        form.assertFormElements("welcome", "5", "true", "60", "30", "60");
        form.saveFormElements("dashboard", "2", "false", "60", "100", "40");

        assertSettingsTable("dashboard", "2", "never", "100", "every 40 seconds");

        assertAndClick("user.settings");
        form.assertFormPresent();
        form.assertFormElements("dashboard", "2", "false", "60", "100", "40");
    }

    public void testEditSettingsCancel()
    {
        assertAndClick("user.settings");

        UserSettingsForm form = new UserSettingsForm(tester);
        form.assertFormPresent();
        form.assertFormElements("welcome", "5", "true", "60", "30", "60");
        form.cancelFormElements("dashboard", "2", "false", null, "10", "30");

        assertSettingsTable("welcome", "5", "every 60 seconds", "30", "every 60 seconds");
    }

    public void testEditSettingsValidation()
    {
        assertAndClick("user.settings");

        UserSettingsForm form = new UserSettingsForm(tester);
        form.assertFormPresent();

        form.assertFormElements("welcome", "5", "true", "60", "30", "60");
        form.saveFormElements("dashboard", "0", "true", "60", "10", "10");
        form.assertFormPresent();
        assertTextPresent("personal build count must be positive");
        form.saveFormElements("dashboard", "5", "true", "0", "10", "10");
        form.assertFormPresent();
        assertTextPresent("refresh interval must be a positive number");
        form.saveFormElements("dashboard", "5", "true", "10", "0", "0");
        form.assertFormPresent();
        assertTextPresent("max lines must be positive");
        assertTextPresent("interval must be positive");
    }

    private void assertAliasesTable(String ...aliases)
    {
        assertTablePresent("aliases");
        ExpectedTable expectedTable = new ExpectedTable();
        expectedTable.appendRow(new ExpectedRow(new String[]{"aliases", "aliases"}));
        expectedTable.appendRow(new ExpectedRow(new String[]{"alias", "actions"}));
        for (String alias : aliases)
        {
            expectedTable.appendRow(new ExpectedRow(new String[]{alias, "delete"}));
        }
        expectedTable.appendRow(new ExpectedRow(new String[]{"add new alias", "add new alias"}));

        assertTableEquals("aliases", expectedTable);
    }

    private void assertSettingsTable(String defaultAction, String myBuildCount, String refreshInterval, String tailLines, String tailInterval)
    {
        assertTablePresent("settings");
        assertTableRowsEqual("settings", 1, new String[][]{
                new String[]{"default page", defaultAction},
                new String[]{"number of personal builds", myBuildCount},
                new String[]{"refresh live content", refreshInterval},
                new String[]{"recipe log tail max lines", tailLines},
                new String[]{"refresh recipe log tail", tailInterval}
        });
    }

    public void testSubscriptionFormValidation()
    {
        // can not create subscription without project
        // can not create subscription without contacts.
        assertLinkNotPresent("subscription.create");

        assertTablePresent("subscriptions");
        assertTableRowsEqual("subscriptions", 1, new String[][]{
                new String[]{"contact", "subscribed to", "actions"},
                new String[]{CONTACT_REQUIRED, CONTACT_REQUIRED, CONTACT_REQUIRED}
        });
    }

    private void createEmailContactPoint(String name, String email)
    {
        EmailContactForm emailForm = emailSetup();
        emailForm.finishFormElements(name, email);
        emailForm.assertFormNotPresent();
        assertTextPresent(name);
        assertTextPresent(email);
    }

    public void testEditContactPoint()
    {
        // create a contact point.
        assertLinkPresentWithText(CREATE_CONTACT_LINK);
        createEmailContactPoint("home", "user@example.com");

        // edit the contact point.
        assertAndClick("edit_home");

        EmailContactForm form = new EmailContactForm(tester, false);
        form.assertFormElements("home", "user@example.com");
        form.saveFormElements("newHome", "anotherUser@example.com");

        // ensure that we have correctly changed the email contact.
        assertLinkPresent("edit_newHome");
        assertLinkNotPresent("edit_home");

        clickLink("edit_newHome");
        form.assertFormPresent();
        form.assertFormElements("newHome", "anotherUser@example.com");
        form.cancelFormElements("cancelled", "nouser@example.com");

        // assert that the contact appears as expected.
        assertTextPresent("newHome");
        assertTextPresent("anotherUser@example.com");
        assertContactsTable("newHome", "anotherUser@example.com");
    }

    private JabberContactForm jabberSetup()
    {
        assertAndClick("contact.create");

        CreateContactForm contactForm = new CreateContactForm(tester);
        contactForm.assertFormPresent();
        assertOptionsEqual("contact", new String[]{ "email", "jabber" });
        contactForm.nextFormElements("jabber");

        JabberContactForm jabberForm = new JabberContactForm(tester, true);
        jabberForm.assertFormPresent();
        return jabberForm;
    }

    public void testAddJabberContactPoint()
    {
        addJabber(JABBER_CONTACT);
        assertContactsTable(JABBER_CONTACT, "jabbername");
    }

    private void addJabber(String name)
    {
        JabberContactForm jabberForm = jabberSetup();
        jabberForm.finishFormElements(name, "jabbername");
    }

    public void testAddJabberContactValidation()
    {
        JabberContactForm jabberForm = jabberSetup();
        jabberForm.finishFormElements("", "");
        jabberForm.assertFormPresent();
        assertTextPresent("name is a required field");
        assertTextPresent("this field is required");
    }

    public void testAddJabberContactCancel()
    {
        JabberContactForm jabberForm = jabberSetup();
        jabberForm.cancelFormElements("hello", "sailor");
        assertTablePresent("contacts");
        assertTextNotPresent("sailor");
    }

    public void testEditJabberContact()
    {
        testAddJabberContactPoint();
        assertAndClick("edit_myjabber");
        JabberContactForm form = new JabberContactForm(tester, false);
        form.assertFormPresent();
        form.saveFormElements("newjabber", "newuid");
        assertContactsTable("newjabber", "newuid");
    }

    public void testEditJabberContactValidation()
    {
        testAddJabberContactPoint();
        assertAndClick("edit_myjabber");
        JabberContactForm form = new JabberContactForm(tester, false);
        form.assertFormPresent();
        form.saveFormElements("", "");
        form.assertFormPresent();
        assertTextPresent("name is a required field");
        assertTextPresent("this field is required");
    }

    public void testEditJabberContactCancel()
    {
        testAddJabberContactPoint();
        assertAndClick("edit_myjabber");
        JabberContactForm form = new JabberContactForm(tester, false);
        form.assertFormPresent();
        form.cancelFormElements("newjabber", "newuid");
        assertContactsTable(JABBER_CONTACT, "jabbername");
    }

    public void testDeleteJabberContact()
    {
        testAddJabberContactPoint();
        assertAndClick("delete_myjabber");
        assertTablePresent("contacts");
        assertTextNotPresent(JABBER_CONTACT);
    }

    // @Requires a project.
    public void testCreateSubscriptionProject()
    {
        addJabber("jabbier");

        clickLink("subscription.create");
        CreateSubscriptionForm form = new CreateSubscriptionForm(tester);
        form.assertFormPresent();
        String contactId = tester.getDialog().getOptionValuesFor("contactId")[0];
        String contactName = tester.getDialog().getOptionsFor("contactId")[0];
        form.nextFormElements(contactId, "project");

        CreateProjectSubscriptionForm projectForm = new CreateProjectSubscriptionForm(tester);
        projectForm.assertFormPresent();
        String projectId = tester.getDialog().getOptionValuesFor("projects")[0];
        String projectName = tester.getDialog().getOptionsFor("projects")[0];
        projectForm.nextFormElements(projectId, "all", null, null, null, null, "simple-instant-message");

        assertSubscriptionsTable(projectName, contactName);
    }

    // @Requires a project.
    public void testCreateSubscriptionAllProjects()
    {
        addJabber("jabbier");

        clickLink("subscription.create");
        CreateSubscriptionForm form = new CreateSubscriptionForm(tester);
        form.assertFormPresent();
        String contactId = tester.getDialog().getOptionValuesFor("contactId")[0];
        String contactName = tester.getDialog().getOptionsFor("contactId")[0];
        form.nextFormElements(contactId, "project");

        CreateProjectSubscriptionForm projectForm = new CreateProjectSubscriptionForm(tester);
        projectForm.assertFormPresent();
        projectForm.nextFormElements("", "all", null, null, null, null, "simple-instant-message");

        assertSubscriptionsTable("[all projects]", contactName);
        clickLinkWithText("edit", 3);
        EditProjectSubscriptionForm edit = new EditProjectSubscriptionForm(tester);
        edit.assertFormElements(contactId, "", "all", null, "5", "builds", "true", "simple-instant-message");
    }

    // @Requires a project.
    public void testCreateSubscriptionPersonal()
    {
        addJabber("jabbier");

        clickLink("subscription.create");
        CreateSubscriptionForm form = new CreateSubscriptionForm(tester);
        form.assertFormPresent();
        String contactId = tester.getDialog().getOptionValuesFor("contactId")[0];
        String contactName = tester.getDialog().getOptionsFor("contactId")[0];
        form.nextFormElements(contactId, "personal");

        CreatePersonalSubscriptionForm personalForm = new CreatePersonalSubscriptionForm(tester);
        personalForm.assertFormPresent();
        personalForm.nextFormElements("html-email");

        assertSubscriptionsTable("personal builds", contactName);
    }

    // @Requires a project.
    public void testEditSubscriptionProject()
    {
        addJabber("zlast");
        testCreateSubscriptionProject();

        clickLinkWithText("edit", 4);
        EditProjectSubscriptionForm form = new EditProjectSubscriptionForm(tester);
        form.assertFormPresent();
        form.assertFormElements(null, null, "all", null, "5", "builds", "true", "simple-instant-message");

        String projectId = tester.getDialog().getOptionValuesFor("projects")[1];
        String projectName = tester.getDialog().getOptionsFor("projects")[1];
        String contactId = tester.getDialog().getOptionValuesFor("contactPointId")[1];
        String contactName = tester.getDialog().getOptionsFor("contactPointId")[1];
        form.saveFormElements(contactId, projectId, "repeated", null, "20", "days", null, "html-email");

        assertSubscriptionsTable(projectName, contactName);
        clickLinkWithText("edit", 4);
        form.assertFormElements(contactId, projectId, "repeated", null, "20", "days", "unsuccessful.count.days(previous) < 20 and unsuccessful.count.days >= 20", "html-email");
    }

    public void testEditSubscriptionPersonal()
    {
        addJabber("zlast");
        testCreateSubscriptionPersonal();

        clickLinkWithText("edit", 4);
        EditPersonalSubscriptionForm form = new EditPersonalSubscriptionForm(tester);
        form.assertFormElements(null, "html-email");

        String contactId = tester.getDialog().getOptionValuesFor("contactPointId")[1];
        String contactName = tester.getDialog().getOptionsFor("contactPointId")[1];
        form.saveFormElements(contactId, "plain-text-email");

        assertSubscriptionsTable("personal builds", contactName);
        clickLinkWithText("edit", 4);
        form.assertFormElements(contactId, "plain-text-email");
    }

    private void assertContactsTable(String name, String uid)
    {
        ExpectedTable expectedTable = new ExpectedTable();
        expectedTable.appendRow(new ExpectedRow(new String[]{"name", "uid", "actions", "actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[]{name, uid, "edit", "delete"}));
        assertTableRowsEqual("contacts", 1, expectedTable);
    }

    private void assertSubscriptionsTable(String project, String contact)
    {
        ExpectedTable expectedTable = new ExpectedTable();
        expectedTable.appendRow(new ExpectedRow(new String[]{"contact", "subscribed to", "actions", "actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[]{contact, project, "edit", "delete"}));
        assertTableRowsEqual("subscriptions", 1, expectedTable);
    }

    private void navigateToPreferences()
    {
        gotoPage("/");
        clickLinkWithText("dashboard");
        clickLinkWithText("preferences");
    }
}
