package com.zutubi.pulse.acceptance;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.acceptance.forms.SignupForm;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.pages.SignupPage;
import com.zutubi.pulse.acceptance.pages.WelcomePage;
import com.zutubi.pulse.acceptance.pages.admin.*;
import com.zutubi.pulse.acceptance.pages.agents.AgentsPage;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.acceptance.pages.dashboard.MyBuildsPage;
import com.zutubi.pulse.acceptance.pages.dashboard.MyPreferencesPage;
import com.zutubi.pulse.acceptance.pages.server.ServerActivityPage;
import com.zutubi.pulse.acceptance.utils.ConfigurationHelper;
import com.zutubi.pulse.acceptance.utils.ConfigurationHelperFactory;
import com.zutubi.pulse.acceptance.utils.SingletonConfigurationHelperFactory;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.user.SignupUserConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;
import static com.zutubi.pulse.master.tove.config.group.ServerPermission.ADMINISTER;
import static com.zutubi.pulse.master.tove.config.group.ServerPermission.CREATE_PROJECT;
import static com.zutubi.tove.type.record.PathUtils.getPath;

public class AnonymousAccessAcceptanceTest extends SeleniumTestBase
{
    private static final Messages SIGNUP_MESSAGES = Messages.getInstance(SignupUserConfiguration.class);
    private static final String ANONYMOUS_GROUP_PATH =  getPath(GROUPS_SCOPE, ANONYMOUS_USERS_GROUP_NAME);

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

        // user access.
        assertRedirectionToLogin(WelcomePage.class);
        assertRedirectionToLogin(BrowsePage.class);
        assertRedirectionToLogin(ServerActivityPage.class);
        assertRedirectionToLogin(AgentsPage.class);
        assertRedirectionToLogin(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);

        // admin access
        assertRedirectionToLogin(UsersPage.class);
        assertRedirectionToLogin(PluginsPage.class);
        assertRedirectionToLogin(QuartzStatisticsPage.class);
    }

    public void testAnonymousSignup() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(true);

        browser.open(BrowsePage.class);
        browser.waitForPageToLoad();
        
        LoginPage loginPage = browser.createPage(LoginPage.class);
        loginPage.waitForSignup();

        String login = "login_" + random;
        String name = "name_" + random;
        String password = "password";

        SignupForm form = loginPage.clickSignup().getForm();
        form.waitFor();
        form.saveFormElements(login, name, password, password);

        WelcomePage welcomePage = browser.createPage(WelcomePage.class);
        welcomePage.waitFor();
        assertTrue(welcomePage.isPresent());
        assertTrue(browser.isTextPresent(name));
        assertTrue(welcomePage.isLogoutLinkPresent());
        assertFalse(welcomePage.isLoginLinkPresent());

        // logout and log back in again.
        browser.logout();

        loginPage = browser.openAndWaitFor(LoginPage.class);
        assertTrue(loginPage.login(login, password));

        welcomePage.waitFor();
        assertTrue(welcomePage.isPresent());
    }

    public void testAnonymousSignupDisabled() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(false);
        LoginPage loginPage = browser.openAndWaitFor(LoginPage.class);
        assertFalse(loginPage.isSignupLinkPresent());

        // go directly to the action and verify that it is disabled.
        SignupPage signupPage = browser.openAndWaitFor(SignupPage.class);
        SignupForm form = signupPage.getForm();
        form.saveFormElements(random, random, "", "");
        assertTrue(browser.isTextPresent("Anonymous signup is not enabled"));
    }

    public void testAnonymousSignupPasswordMismatch() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(true);

        LoginPage login = browser.openAndWaitFor(LoginPage.class);
        SignupForm form = login.clickSignup().getForm();
        form.waitFor();
        form.saveFormElements(random, random, "p1", "p2");
        assertTrue(form.isFormPresent());
        assertTrue(browser.isTextPresent(SIGNUP_MESSAGES.format("passwords.differ")));
    }

    public void testAnonymousSignupExistingUser() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(true);

        String adminUsername = ADMIN_CREDENTIALS.getUserName();

        LoginPage login = browser.openAndWaitFor(LoginPage.class);
        SignupForm form = login.clickSignup().getForm();
        form.waitFor();
        form.saveFormElements(adminUsername, "name", "p", "p");
        assertTrue(form.isFormPresent());
        assertTrue(browser.isTextPresent("login '"+adminUsername+"' is already in use"));
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

        // user access
        assertRedirectionToLogin(DashboardPage.class);
        assertRedirectionToLogin(MyBuildsPage.class);
        assertRedirectionToLogin(MyPreferencesPage.class);

        // admin access
        assertRedirectionToLogin(UsersPage.class);
        assertRedirectionToLogin(PluginsPage.class);
        assertRedirectionToLogin(QuartzStatisticsPage.class);

        // can see configured projects and agents.
        assertNoRedirectionToLogin(urls.adminAgents());
        assertNoRedirectionToLogin(urls.adminProjects());
    }

    public void testAssignServerPermissionToAnonymousUsers() throws Exception
    {
        setAnonymousAccess(true);
        setAnonymousSignup(false);

        setAnonymousServerPermissions();

        ProjectHierarchyPage hierarchyPage = browser.open(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);
        browser.waitForPageToLoad();
        assertFalse(hierarchyPage.isAddPresent());

        setAnonymousServerPermissions(CREATE_PROJECT);
        hierarchyPage.openAndWaitFor();
        assertTrue(hierarchyPage.isAddPresent());

        assertRedirectionToLogin(GroupsPage.class);
        assertRedirectionToLogin(UsersPage.class);

        setAnonymousServerPermissions(ADMINISTER);
        hierarchyPage.openAndWaitFor();
        assertTrue(hierarchyPage.isAddPresent());

        assertPageVisible(GroupsPage.class);
        assertPageVisible(UsersPage.class);
    }

    private void setAnonymousServerPermissions(ServerPermission... permissions) throws Exception
    {
        Hashtable<String, Object> group = xmlRpcHelper.getConfig(ANONYMOUS_GROUP_PATH);
        List<String> permissionStrings = CollectionUtils.map(permissions, new Mapping<ServerPermission, String>()
        {
            public String map(ServerPermission permission)
            {
                return permission.toString();
            }
        });
        group.put("serverPermissions", new Vector(permissionStrings));
        xmlRpcHelper.saveConfig(ANONYMOUS_GROUP_PATH, group, false);
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

    private <T extends SeleniumPage> void assertRedirectionToLogin(Class<T> pageType, Object... extraArgs)
    {
        SeleniumPage page = browser.createPage(pageType, extraArgs);
        assertRedirectionToLogin(page.getUrl());
    }

    private <T extends SeleniumPage> void assertPageVisible(Class<T> pageType, Object... extraArgs)
    {
        SeleniumPage page = browser.openAndWaitFor(pageType, extraArgs);
        assertTrue(page.isPresent());
    }

    private void assertRedirectionToLogin(String url)
    {
        browser.open(url);
        browser.waitForPageToLoad();

        // We should be denied access and redirected to the login page.
        LoginPage loginPage = browser.createPage(LoginPage.class);
        loginPage.waitFor();
        assertTrue(loginPage.isPresent());
    }

    private void assertNoRedirectionToLogin(String url)
    {
        browser.open(url);
        browser.waitForPageToLoad();

        LoginPage loginPage = browser.createPage(LoginPage.class);
        assertFalse(loginPage.isPresent());
    }
}
