package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.pages.WelcomePage;
import com.zutubi.pulse.acceptance.pages.dashboard.PreferencesPage;
import com.zutubi.pulse.master.tove.config.user.SetOwnPasswordConfiguration;
import com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfigurationActions;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfigurationActions;
import com.zutubi.util.RandomUtils;

import java.util.Hashtable;

/**
 * Acceptance tests for the users preferences.
 */
public class UserPreferencesAcceptanceTest extends SeleniumTestBase
{
    private String login;
    private String userPath;

    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        login = RandomUtils.randomString(10);
        userPath = xmlRpcHelper.insertTrivialUser(login);
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testSetPassword() throws Exception
    {
        PreferencesPage preferencesPage = setupUserPasswordAndGoToPreferences();

        preferencesPage.clickAction(UserPreferencesConfigurationActions.ACTION_SET_PASSWORD);
        ConfigurationForm setPasswordForm = new ConfigurationForm(selenium, SetOwnPasswordConfiguration.class);
        setPasswordForm.waitFor();

        String newPassword = random + "new";
        setPasswordForm.saveFormElements("x", newPassword, newPassword);
        assertTrue(setPasswordForm.isFormPresent());
        assertTextPresent("current password is incorrect");

        setPasswordForm.saveFormElements(random, "foo", "bar");
        assertTrue(setPasswordForm.isFormPresent());
        assertTextPresent("passwords do not match");

        setPasswordForm.saveFormElements(random, newPassword, newPassword);

        preferencesPage.waitFor();
        logout();
        
        login(login, newPassword);
        WelcomePage welcomePage = new WelcomePage(selenium, urls);
        welcomePage.waitFor();
    }

    public void testSetPasswordTwiceInSameSession() throws Exception
    {
        PreferencesPage preferencesPage = setupUserPasswordAndGoToPreferences();

        preferencesPage.clickAction(UserPreferencesConfigurationActions.ACTION_SET_PASSWORD);
        ConfigurationForm setPasswordForm = new ConfigurationForm(selenium, SetOwnPasswordConfiguration.class);
        setPasswordForm.waitFor();

        String newPassword = random + "new";
        setPasswordForm.saveFormElements(random, newPassword, newPassword);
        preferencesPage.waitFor();

        preferencesPage.clickAction(UserPreferencesConfigurationActions.ACTION_SET_PASSWORD);
        setPasswordForm.waitFor();

        String newerPassword = newPassword + "er";
        setPasswordForm.saveFormElements(newPassword, newerPassword, newerPassword);
        preferencesPage.waitFor();
        
        logout();
        login(login, newerPassword);
        WelcomePage welcomePage = new WelcomePage(selenium, urls);
        welcomePage.waitFor();
    }

    private PreferencesPage setupUserPasswordAndGoToPreferences() throws Exception
    {
        Hashtable<String, Object> password = xmlRpcHelper.createEmptyConfig(SetPasswordConfiguration.class);
        password.put("password", random);
        password.put("confirmPassword", random);
        xmlRpcHelper.doConfigActionWithArgument(userPath, UserConfigurationActions.ACTION_SET_PASSWORD, password);

        login(login, random);

        PreferencesPage preferencesPage = new PreferencesPage(selenium, urls, login);
        preferencesPage.goTo();
        return preferencesPage;
    }
}