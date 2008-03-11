package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.pulse.acceptance.forms.admin.SetPasswordForm;
import com.zutubi.pulse.acceptance.forms.admin.UserForm;
import com.zutubi.pulse.acceptance.pages.WelcomePage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;

/**
 * Acceptance tests for actions that may be executed on configuration
 * instances.
 */
public class ConfigActionsAcceptanceTest extends SeleniumTestBase
{
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

    public void testCustomActionWithArgument() throws Exception
    {
        ListPage usersPage = customActionWithArgumentPrelude();

        SetPasswordForm form = new SetPasswordForm(selenium);
        form.waitFor();
        form.saveFormElements("testpw", "testpw");

        usersPage.waitFor();
        logout();

        // Login with the new password
        login(random, "testpw");
        selenium.waitForPageToLoad("30000");
        WelcomePage welcomePage = new WelcomePage(selenium, urls);
        welcomePage.assertPresent();
    }

    public void testCustomActionWithArgumentValidation() throws Exception
    {
        customActionWithArgumentPrelude();
        SetPasswordForm form = new SetPasswordForm(selenium);
        form.waitFor();
        form.saveFormElements("one", "two");
        form.assertFormPresent();
        assertTextPresent("passwords do not match");
    }

    public void testCustomActionWithArgumentCancel() throws Exception
    {
        customActionWithArgumentPrelude();

        SetPasswordForm setPasswordForm = new SetPasswordForm(selenium);
        setPasswordForm.waitFor();
        setPasswordForm.cancelFormElements("testpw", "testpw");

        UserForm userForm = new UserForm(selenium, random);
        userForm.waitFor();
        logout();

        // Check the password is unchanged
        login(random, "");
        selenium.waitForPageToLoad("30000");
        WelcomePage welcomePage = new WelcomePage(selenium, urls);
        welcomePage.assertPresent();
    }

    private ListPage customActionWithArgumentPrelude() throws Exception
    {
        xmlRpcHelper.insertTrivialUser(random);

        loginAsAdmin();
        ListPage usersPage = new ListPage(selenium, urls, ConfigurationRegistry.USERS_SCOPE);
        usersPage.goTo();
        usersPage.clickAction(random, "setPassword");
        return usersPage;
    }
}
