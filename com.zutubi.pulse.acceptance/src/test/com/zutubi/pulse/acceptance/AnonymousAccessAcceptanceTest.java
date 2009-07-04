package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.SignupForm;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.zutubi.pulse.acceptance.pages.WelcomePage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

public class AnonymousAccessAcceptanceTest extends SeleniumTestBase
{
    private static final String ANONYMOUS_GROUP_PATH = "groups/anonymous users";

    private static final String KEY_ANONYMOUS_ACCESS = "anonymousAccessEnabled";
    private static final String KEY_ANONYMOUS_SIGNUP = "anonymousSignupEnabled";

    private static final String ID_LOGIN         = "login";
    private static final String ID_DASHBOARD_TAB = "tab.dashboard";
    private static final String ID_PREFERENCES   = "prefs";
    private static final String ID_LOGOUT        = "logout";

    private static final String SIGNUP_INPUT_ACTION = "signup!input.action";

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testNoAnonymousAccess() throws Exception
    {
        ensureSetting(KEY_ANONYMOUS_ACCESS, false);
        browser.open(BrowsePage.class);

        // We should be denied access and redirected to the login page.
        LoginPage loginPage = browser.createPage(LoginPage.class);
        loginPage.waitFor();
        assertTitle(loginPage);
    }

    public void testAnonymousSignup() throws Exception
    {
        ensureSetting(KEY_ANONYMOUS_ACCESS, false);
        ensureSetting(KEY_ANONYMOUS_SIGNUP, true);

        LoginPage loginPage = browser.open(LoginPage.class);
        loginPage.waitForSignup();

        SignupForm form = loginPage.clickSignup();
        form.waitFor();
        form.saveFormElements("login_" + random, "name_" + random, "password", "password");

        WelcomePage welcomePage = browser.createPage(WelcomePage.class);
        welcomePage.waitFor();
        assertTitle(welcomePage);
        assertTextPresent("name_" + random);
        assertElementPresent(ID_DASHBOARD_TAB);
        assertElementPresent(ID_PREFERENCES);
        assertElementPresent(ID_LOGOUT);
        assertElementNotPresent(ID_LOGIN);
    }

    public void testAnonymousSignupDisabled() throws Exception
    {
        ensureSetting(KEY_ANONYMOUS_SIGNUP, false);
        LoginPage loginPage = browser.openAndWaitFor(LoginPage.class);
        assertFalse(loginPage.isSignupPresent());
        browser.open(SIGNUP_INPUT_ACTION);
        SignupForm form = browser.createForm(SignupForm.class);
        assertTrue(form.isFormPresent());
        form.saveFormElements(random, random, "", "");
        assertTextPresent("Anonymous signup is not enabled");
    }

    public void testAnonymousSingupPasswordMismatch() throws Exception
    {
        ensureSetting(KEY_ANONYMOUS_SIGNUP, true);
        browser.open(SIGNUP_INPUT_ACTION);
        SignupForm form = browser.createForm(SignupForm.class);
        form.waitFor();
        form.saveFormElements(random, random, "p1", "p2");
        assertTrue(form.isFormPresent());
        assertTextPresent("passwords do not match");
    }

    public void testAnonymousSingupExistingUser() throws Exception
    {
        ensureSetting(KEY_ANONYMOUS_SIGNUP, true);
        browser.open(SIGNUP_INPUT_ACTION);
        SignupForm form = browser.createForm(SignupForm.class);
        form.waitFor();
        form.saveFormElements("admin", "name", "p", "p");
        assertTrue(form.isFormPresent());
        assertTextPresent("login 'admin' is already in use");
    }

    public void testAnonymousAccess() throws Exception
    {
        ensureSetting(KEY_ANONYMOUS_ACCESS, true);

        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertTitle(browsePage);
        assertElementPresent(ID_LOGIN);

        // No dashboard tab, user info or logout link for anonymous users
        assertElementNotPresent(ID_DASHBOARD_TAB);
        assertElementNotPresent(ID_PREFERENCES);
        assertElementNotPresent(ID_LOGOUT);
    }

    public void testAssignServerPermissionToAnonymousUsers() throws Exception
    {
        ensureSetting(KEY_ANONYMOUS_ACCESS, true);
        Hashtable<String, Object> group = xmlRpcHelper.getConfig(ANONYMOUS_GROUP_PATH);
        group.put("serverPermissions", new Vector(0));
        xmlRpcHelper.saveConfig(ANONYMOUS_GROUP_PATH, group, false);

        browser.newSession();
        
        ProjectHierarchyPage hierarchyPage = browser.open(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        assertFalse(hierarchyPage.isAddPresent());

        group.put("serverPermissions", new Vector<String>(Arrays.asList(ServerPermission.CREATE_PROJECT.toString())));
        xmlRpcHelper.saveConfig(ANONYMOUS_GROUP_PATH, group, false);
        
        browser.newSession();
        hierarchyPage.openAndWaitFor();
        browser.waitForElement(ProjectHierarchyPage.LINK_ADD);
    }

    private void ensureSetting(String key, boolean enabled) throws Exception
    {
        Hashtable<String, Object> general = xmlRpcHelper.getConfig(GlobalConfiguration.SCOPE_NAME);
        if((Boolean)general.get(key) != enabled)
        {
            general.put(key, enabled);
            xmlRpcHelper.saveConfig(GlobalConfiguration.SCOPE_NAME, general, false);
            browser.newSession();
        }
    }
}
