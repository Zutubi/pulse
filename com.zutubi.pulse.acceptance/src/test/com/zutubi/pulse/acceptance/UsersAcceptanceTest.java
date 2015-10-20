package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LoginForm;
import com.zutubi.pulse.acceptance.forms.admin.AddUserForm;
import com.zutubi.pulse.acceptance.forms.dashboard.ChangePasswordForm;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.UsersPage;
import com.zutubi.pulse.acceptance.pages.dashboard.PreferencesPage;
import com.zutubi.pulse.master.rest.wizards.UserConfigurationCreator;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfigurationActions;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfigurationActions;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfigurationStateDisplay;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;

import static com.zutubi.util.CollectionUtils.asPair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class UsersAcceptanceTest extends AcceptanceTestBase
{
    private static final String STATE_LAST_ACCESS = "lastAccess";
    private static final String ACCESS_NEVER = "never";
    private static final String ACCESS_NOW = "less than 1 second ago";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.deleteAllConfigs(PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testActiveUsers() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialUser(random);

        getBrowser().loginAsAdmin();
        UsersPage usersPage = getBrowser().openAndWaitFor(UsersPage.class);

        assertTrue(usersPage.isActiveCountPresent());
        assertEquals("1 of 2 users have been active in the last 10 minutes", usersPage.getActiveCount());

        getBrowser().logout();
        getBrowser().loginAndWait(random, "");
        getBrowser().logout();

        getBrowser().loginAsAdmin();
        usersPage.openAndWaitFor();
        assertEquals("2 of 2 users have been active in the last 10 minutes", usersPage.getActiveCount());
    }

    public void testLastAccessTime() throws Exception
    {
        String userPath = rpcClient.RemoteApi.insertTrivialUser(random);

        getBrowser().loginAsAdmin();
        CompositePage userPage = getBrowser().openAndWaitFor(CompositePage.class, userPath);

        assertTrue(userPage.isStateFieldPresent(STATE_LAST_ACCESS));
        assertEquals(ACCESS_NEVER, userPage.getStateField(STATE_LAST_ACCESS));

        getBrowser().logout();
        getBrowser().loginAndWait(random, "");
        getBrowser().logout();

        getBrowser().loginAsAdmin();
        userPage.openAndWaitFor();
        assertFalse(userPage.getStateField(STATE_LAST_ACCESS).equals(ACCESS_NEVER));
    }

    public void testLastAccessTimeForSelf()
    {
        getBrowser().loginAsAdmin();
        CompositePage userPage = getBrowser().openAndWaitFor(CompositePage.class, "users/admin");

        assertTrue(userPage.isStateFieldPresent(STATE_LAST_ACCESS));
        assertEquals(ACCESS_NOW, userPage.getStateField(STATE_LAST_ACCESS));

        getBrowser().logout();
    }

    public void testPrimaryContactPointCreation()
    {
        String contactPath = createUserWithContact(random);

        CompositePage emailContactPage = getBrowser().openAndWaitFor(CompositePage.class, contactPath);
        assertPrimaryContact(emailContactPage);
    }

    public void testPrimaryContactPointCannotBeDeleted()
    {
        String contactPath = createUserWithContact(random);
        try
        {
            rpcClient.RemoteApi.deleteConfig(contactPath);
            fail("Should not be able to delete primary contact");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("permanent"));
        }
    }

    public void testMarkContactPrimary() throws Exception
    {
        final String NAME_ANOTHER = "another";

        String originalContactPath = createUserWithContact(random);
        String contactsPath = PathUtils.getParentPath(originalContactPath);
        Hashtable<String,Object> emailConfig = rpcClient.RemoteApi.createDefaultConfig(EmailContactConfiguration.class);
        emailConfig.put("name", NAME_ANOTHER);
        emailConfig.put("address", "another@example.com");
        rpcClient.RemoteApi.insertConfig(contactsPath, emailConfig);

        CompositePage emailContactPage = getBrowser().openAndWaitFor(CompositePage.class, PathUtils.getPath(contactsPath, NAME_ANOTHER));
        assertNotPrimaryContact(emailContactPage);

        emailContactPage.clickActionAndWait(ContactConfigurationActions.ACTION_MARK_PRIMARY);
        assertPrimaryContact(emailContactPage);

        emailContactPage = getBrowser().openAndWaitFor(CompositePage.class, originalContactPath);
        assertNotPrimaryContact(emailContactPage);
    }
    
    public void testChangePassword() throws Exception
    {
        final String NEW_PASSWORD = "boo";

        rpcClient.RemoteApi.insertTrivialUser(random);
        
        getBrowser().loginAndWait(random, "");
        
        PreferencesPage preferencesPage = getBrowser().openAndWaitFor(PreferencesPage.class, random);
        preferencesPage.clickAction(UserPreferencesConfigurationActions.ACTION_CHANGE_PASSWORD);
        
        ChangePasswordForm changePasswordForm = getBrowser().createForm(ChangePasswordForm.class);
        changePasswordForm.waitFor();

        changePasswordForm.saveFormElements("nope", NEW_PASSWORD, NEW_PASSWORD);
        changePasswordForm.waitFor();
        getBrowser().waitForTextPresent("password is incorrect");

        changePasswordForm.saveFormElements("", NEW_PASSWORD, "wrong");
        changePasswordForm.waitFor();
        getBrowser().waitForTextPresent("new passwords do not match");

        changePasswordForm.saveFormElements("", NEW_PASSWORD, NEW_PASSWORD);
        getBrowser().waitForStatus("password changed");
        getBrowser().logout();

        LoginPage loginPage = getBrowser().openAndWaitFor(LoginPage.class);
        LoginForm loginForm = getBrowser().createForm(LoginForm.class);
        loginForm.submitNamedFormElements("login", asPair(LoginPage.FIELD_USERNAME, random), asPair(LoginPage.FIELD_PASSWORD, ""));
        loginForm.waitFor();
        
        loginPage.openAndWaitFor();
        loginPage.login(random, NEW_PASSWORD);
        assertTrue(getBrowser().isLoggedIn());
    }

    private String createUserWithContact(String name)
    {
        getBrowser().loginAsAdmin();
        UsersPage usersPage = getBrowser().openAndWaitFor(UsersPage.class);
        usersPage.clickAdd();
        AddUserForm addUserForm = getBrowser().createForm(AddUserForm.class);
        addUserForm.waitFor();
        String email = name + "@example.com";
        addUserForm.finishNamedFormElements(asPair("login", name), asPair("name", name), asPair("emailAddress", email));
        return PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, name, "preferences", "contacts", UserConfigurationCreator.CONTACT_NAME);
    }

    private void assertPrimaryContact(CompositePage emailContactPage)
    {
        assertTrue(emailContactPage.isStatePresent());
        assertTrue(emailContactPage.isStateFieldPresent(ContactConfigurationStateDisplay.FIELD_PRIMARY));
        assertFalse(emailContactPage.isActionPresent(ContactConfigurationActions.ACTION_MARK_PRIMARY));
        assertFalse(emailContactPage.isActionPresent(AccessManager.ACTION_DELETE));
    }

    private void assertNotPrimaryContact(CompositePage emailContactPage)
    {
        assertFalse(emailContactPage.isStatePresent());
        assertTrue(emailContactPage.isActionPresent(ContactConfigurationActions.ACTION_MARK_PRIMARY));
        assertTrue(emailContactPage.isActionPresent(AccessManager.ACTION_DELETE));
    }
}
