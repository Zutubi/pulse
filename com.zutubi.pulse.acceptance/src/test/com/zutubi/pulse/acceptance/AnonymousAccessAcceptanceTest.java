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
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.acceptance.pages.dashboard.MyBuildsPage;
import com.zutubi.pulse.acceptance.pages.dashboard.MyPreferencesPage;
import com.zutubi.pulse.acceptance.pages.server.ServerActivityPage;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.user.SignupUserConfiguration;
import com.zutubi.tove.type.record.PathUtils;
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

public class AnonymousAccessAcceptanceTest extends AcceptanceTestBase
{
    private static final Messages SIGNUP_MESSAGES = Messages.getInstance(SignupUserConfiguration.class);
    private static final String ANONYMOUS_GROUP_PATH =  getPath(GROUPS_SCOPE, ANONYMOUS_USERS_GROUP_NAME);
    private static final String PROPERTY_ANON_ACCESS =  "anonymousAccessEnabled";
    private static final String PROPERTY_ANON_SIGNUP =  "anonymousSignupEnabled";

    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(false);
        setAnonymousServerPermissions();
        rpcClient.logout();
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

        getBrowser().open(BrowsePage.class);

        LoginPage loginPage = getBrowser().createPage(LoginPage.class);
        loginPage.waitForSignup();

        String login = "login_" + random;
        String name = "name_" + random;
        String password = "password";

        SignupForm form = loginPage.clickSignup().getForm();
        form.waitFor();
        form.saveFormElements(login, name, password, password);

        WelcomePage welcomePage = getBrowser().createPage(WelcomePage.class);
        welcomePage.waitFor();
        assertTrue(welcomePage.isPresent());
        getBrowser().waitForTextPresent(name);
        assertTrue(welcomePage.isLogoutLinkPresent());
        assertFalse(welcomePage.isLoginLinkPresent());

        // logout and log back in again.
        getBrowser().logout();

        loginPage = getBrowser().openAndWaitFor(LoginPage.class);
        loginPage.login(login, password);
        assertTrue(getBrowser().isLoggedIn());

        welcomePage.waitFor();
        assertTrue(welcomePage.isPresent());
    }

    public void testAnonymousSignupDisabled() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(false);
        LoginPage loginPage = getBrowser().openAndWaitFor(LoginPage.class);
        assertFalse(loginPage.isSignupLinkPresent());

        // go directly to the action and verify that it is disabled.
        SignupPage signupPage = getBrowser().openAndWaitFor(SignupPage.class);
        SignupForm form = signupPage.getForm();
        form.saveFormElements(random, random, "", "");
        getBrowser().waitForTextPresent("Anonymous signup is not enabled");
    }

    public void testAnonymousSignupPasswordMismatch() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(true);

        LoginPage login = getBrowser().openAndWaitFor(LoginPage.class);
        SignupForm form = login.clickSignup().getForm();
        form.waitFor();
        form.saveFormElements(random, random, "p1", "p2");
        form.waitFor();
        getBrowser().waitForTextPresent(SIGNUP_MESSAGES.format("passwords.differ"));
    }

    public void testAnonymousSignupExistingUser() throws Exception
    {
        setAnonymousAccess(false);
        setAnonymousSignup(true);

        String adminUsername = ADMIN_CREDENTIALS.getUserName();

        LoginPage login = getBrowser().openAndWaitFor(LoginPage.class);
        SignupForm form = login.clickSignup().getForm();
        form.waitFor();
        form.saveFormElements(adminUsername, "name", "p", "p");
        form.waitFor();
        getBrowser().waitForTextPresent("login '" + adminUsername + "' is already in use");
    }

    public void testAnonymousAccess() throws Exception
    {
        setAnonymousAccess(true);
        setAnonymousSignup(false);

        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertEquals(getBrowser().getTitle(), browsePage.getTitle());
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

        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);
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

        setAnonymousServerPermissions();
    }

    public void testAssignTriggerPermissionToAnonymousUsers() throws Exception
    {
        setAnonymousAccess(true);
        setAnonymousSignup(false);

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random);
        String permissionsPath = PathUtils.getPath(projectPath, Constants.Project.PERMISSIONS);
        Hashtable<String, Object> acl = rpcClient.RemoteApi.createDefaultConfig(ProjectAclConfiguration.class);
        acl.put("group", ANONYMOUS_GROUP_PATH);
        acl.put("allowedActions", new String[]{ProjectConfigurationActions.ACTION_TRIGGER});
        rpcClient.RemoteApi.insertConfig(permissionsPath, acl);

        getBrowser().newSession();

        ProjectHomePage homePage = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        assertTrue(homePage.isTriggerActionPresent());
        homePage.triggerBuild();

        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);
    }

    private void setAnonymousServerPermissions(ServerPermission... permissions) throws Exception
    {
        Hashtable<String, Object> group = rpcClient.RemoteApi.getConfig(ANONYMOUS_GROUP_PATH);
        List<String> permissionStrings = CollectionUtils.map(permissions, new Mapping<ServerPermission, String>()
        {
            public String map(ServerPermission permission)
            {
                return permission.toString();
            }
        });
        group.put("serverPermissions", new Vector<String>(permissionStrings));
        rpcClient.RemoteApi.saveConfig(ANONYMOUS_GROUP_PATH, group, false);
    }

    private void setAnonymousAccess(boolean enabled) throws Exception
    {
        setBooleanConfig(PROPERTY_ANON_ACCESS, enabled);
    }

    private void setAnonymousSignup(boolean enabled) throws Exception
    {
        setBooleanConfig(PROPERTY_ANON_SIGNUP, enabled);
    }

    private void setBooleanConfig(String property, boolean enabled) throws Exception
    {
        Hashtable<String, Object> global = rpcClient.RemoteApi.getConfig(GlobalConfiguration.SCOPE_NAME);
        if ((Boolean) global.get(property) != enabled)
        {
            global.put(property, enabled);
            rpcClient.RemoteApi.saveConfig(GlobalConfiguration.SCOPE_NAME, global, false);
            getBrowser().newSession();
        }
    }

    private <T extends SeleniumPage> void assertRedirectionToLogin(Class<T> pageType, Object... extraArgs)
    {
        SeleniumPage page = getBrowser().createPage(pageType, extraArgs);
        assertRedirectionToLogin(page.getUrl());
    }

    private <T extends SeleniumPage> void assertPageVisible(Class<T> pageType, Object... extraArgs)
    {
        SeleniumPage page = getBrowser().openAndWaitFor(pageType, extraArgs);
        assertTrue(page.isPresent());
    }

    private void assertRedirectionToLogin(String url)
    {
        getBrowser().open(url);

        // We should be denied access and redirected to the login page.
        LoginPage loginPage = getBrowser().createPage(LoginPage.class);
        loginPage.waitFor();
    }

    private void assertNoRedirectionToLogin(String url)
    {
        getBrowser().open(url);

        LoginPage loginPage = getBrowser().createPage(LoginPage.class);
        assertFalse(loginPage.isPresent());
    }
}
