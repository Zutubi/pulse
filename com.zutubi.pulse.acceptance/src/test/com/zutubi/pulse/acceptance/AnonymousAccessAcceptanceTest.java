package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.SignupForm;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.zutubi.pulse.acceptance.pages.WelcomePage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.utils.ConfigurationHelper;
import com.zutubi.pulse.acceptance.utils.ConfigurationHelperFactory;
import com.zutubi.pulse.acceptance.utils.SingletonConfigurationHelperFactory;
import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

public class AnonymousAccessAcceptanceTest extends SeleniumTestBase
{
    private static final String ANONYMOUS_GROUP_PATH = "groups/anonymous users";

    private static final String SIGNUP_INPUT_ACTION = "signup!input.action";
    private ConfigurationHelper configurationHelper;

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testNoAnonymousAccess() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(false);
        browser.open(BrowsePage.class);

        // We should be denied access and redirected to the login page.
        LoginPage loginPage = browser.createPage(LoginPage.class);
        loginPage.waitFor();
        assertTitle(loginPage);
    }

    public void testAnonymousSignup() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(true);

        LoginPage loginPage = browser.open(LoginPage.class);
        loginPage.waitForSignup();

        String login = "login_" + random;
        String password = "password";

        SignupForm form = loginPage.clickSignup();
        form.waitFor();
        form.saveFormElements(login, "name_" + random, password, password);

        WelcomePage welcomePage = browser.createPage(WelcomePage.class);
        welcomePage.waitFor();
        assertTitle(welcomePage);
        assertTextPresent("name_" + random);
        assertTrue(welcomePage.isElementIdPresent(IDs.ID_DASHBOARD_TAB));
        assertTrue(welcomePage.isElementIdPresent(IDs.ID_PREFERENCES));
        assertTrue(welcomePage.isElementIdPresent(IDs.ID_LOGOUT));
        assertFalse(welcomePage.isElementIdPresent(IDs.ID_LOGIN));

        // logout and log back in again.
        browser.logout();

        loginPage = browser.openAndWaitFor(LoginPage.class);
        assertTrue(loginPage.login(login, password));

        welcomePage.waitFor();
        assertTitle(welcomePage);
    }

    public void testAnonymousSignupDisabled() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(false);
        LoginPage loginPage = browser.openAndWaitFor(LoginPage.class);
        assertFalse(loginPage.isSignupPresent());

        // go directly to the action and verify that it is disabled.
        browser.open(urls.base() + SIGNUP_INPUT_ACTION);
        SignupForm form = browser.createForm(SignupForm.class);
        assertTrue(form.isFormPresent());
        form.saveFormElements(random, random, "", "");
        assertTextPresent("Anonymous signup is not enabled");
    }

    public void testAnonymousSingupPasswordMismatch() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(true);
        browser.open(urls.base() + SIGNUP_INPUT_ACTION);
        SignupForm form = browser.createForm(SignupForm.class);
        form.waitFor();
        form.saveFormElements(random, random, "p1", "p2");
        assertTrue(form.isFormPresent());
        assertTextPresent("passwords do not match");
    }

    public void testAnonymousSingupExistingUser() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(true);
        browser.open(urls.base() + SIGNUP_INPUT_ACTION);
        SignupForm form = browser.createForm(SignupForm.class);
        form.waitFor();
        form.saveFormElements(ADMIN_CREDENTIALS.getUserName(), "name", "p", "p");
        assertTrue(form.isFormPresent());
        assertTextPresent("login 'admin' is already in use");
    }

    public void testAnonymousAccess() throws Exception
    {
        setAnonymousAccess(true);
        setAnonymousSignup(false);

        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertTitle(browsePage);
        assertTrue(browsePage.isElementIdPresent(IDs.ID_LOGIN));

        // No dashboard tab, user info or logout link for anonymous users
        assertFalse(browsePage.isElementIdPresent(IDs.ID_DASHBOARD_TAB));
        assertFalse(browsePage.isElementIdPresent(IDs.ID_PREFERENCES));
        assertFalse(browsePage.isElementIdPresent(IDs.ID_LOGOUT));
    }

    public void testAssignServerPermissionToAnonymousUsers() throws Exception
    {
        setAnonymousAccess(true);
        setAnonymousSignup(false);
        Hashtable<String, Object> group = xmlRpcHelper.getConfig(ANONYMOUS_GROUP_PATH);
        group.put("serverPermissions", new Vector(0));
        xmlRpcHelper.saveConfig(ANONYMOUS_GROUP_PATH, group, false);

        browser.newSession();

        ProjectHierarchyPage hierarchyPage = browser.open(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        browser.waitForPageToLoad();
        assertFalse(hierarchyPage.isAddPresent());

        group.put("serverPermissions", new Vector<String>(Arrays.asList(ServerPermission.CREATE_PROJECT.toString())));
        xmlRpcHelper.saveConfig(ANONYMOUS_GROUP_PATH, group, false);

        browser.newSession();
        hierarchyPage.openAndWaitFor();
        browser.waitForElement(ProjectHierarchyPage.LINK_ADD);
    }

    private void setAnonymousAccess(boolean enabled) throws Exception
    {
        GlobalConfiguration globalConfig = configurationHelper.getConfiguration(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
        if (globalConfig.isAnonymousAccessEnabled() != enabled)
        {
            globalConfig.setAnonymousAccessEnabled(enabled);
            configurationHelper.update(globalConfig, false);
            browser.newSession();
        }
    }

    private void setAnonymousSignup(boolean enabled) throws Exception
    {
        GlobalConfiguration globalConfig = configurationHelper.getConfiguration(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
        if (globalConfig.isAnonymousSignupEnabled() != enabled)
        {
            globalConfig.setAnonymousSignupEnabled(enabled);
            configurationHelper.update(globalConfig, false);
            browser.newSession();
        }
    }
}
