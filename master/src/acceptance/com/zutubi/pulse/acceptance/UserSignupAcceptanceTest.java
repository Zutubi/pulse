package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.GeneralConfigurationForm;
import com.zutubi.pulse.acceptance.forms.SignupForm;
import com.zutubi.pulse.util.RandomUtils;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * This acceptance test case covers the various aspects of an annonymous user using the signup functionality
 * to create an account for themselves.
 *
 */
public class UserSignupAcceptanceTest extends BaseAcceptanceTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();

        ensureLoggedOut();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test that the annonymous signup option is available in the general administration panel.
     */
    public void testCanEnableDisableAnnonymousSignup()
    {
        loginAsAdmin();
        navigateToGeneralConfiguration();

        // assert field in table.
        assertTextInTable("general.config", "anonymous signup");

        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.setCheckboxChecked("signupEnabled", true);
        form.save();

        // assert that the form submission was successful.
        form.assertFormNotPresent();

        // assert that the state of annonymous signup is as expected.
        assertTableRowEqual("general.config", 5, new String[]{"anonymous signup", "enabled"});

        // disable it again.
        clickLink("general.edit");
        form.setCheckboxChecked("signupEnabled", false);
        form.save();
        form.assertFormNotPresent();

        assertTableRowEqual("general.config", 5, new String[]{"anonymous signup", "disabled"});   
    }

    private void enableAnonymousSignup()
    {
        setAnonymousSignup(true);
    }

    private void disableAnonymousSignup()
    {
        setAnonymousSignup(false);
    }

    private void setAnonymousSignup(boolean b)
    {
        // maybe this is where 'switching' users would come in handy...
        loginAsAdmin();
        navigateToGeneralConfiguration();

        clickLink("general.edit");

        // ensure that the initial value is false.
        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.setCheckboxChecked("signupEnabled", b);
        form.save();

        // assert that the form submission was successful.
        form.assertFormNotPresent();

        logout();
    }

    /**
     * Test that annonymous users can create accounts iff this feature is enabled.
     *
     * a) check that the links are only visible when enabled.
     * b) check that the actions only work when enabled.
     *
     * @throws Exception if an unexpected error occurs in this test.
     */
    public void testEnableDisableAnnonymousSignup() throws Exception
    {
        // disable annonymous signup.
        disableAnonymousSignup();

        // verify link does not appear on login page.
        beginAt("/login.action");
        assertLinkNotPresent("signup");

        // verify that the form is not available.
        goTo("/signup!input.action");
        SignupForm form = new SignupForm(tester);
        form.assertFormNotPresent();

        // verify that action can not be posted.
        goTo("/signup.action?newUser.login=123qweasdzxc&newUser.name=Spam&newUser.password=spam&confirm=spam");
        assertTextPresent("Anonymous signup is currently disabled");

        // enable annonymous signup.
        enableAnonymousSignup();

        // verify link does appear on login page.
        beginAt("/login.action");
        assertLinkPresent("signup");
        
    }

    /**
     * Test that the validation on the form works as expected.
     *
     * a) the passwords are checked and match.
     * b) the user name is not already in use.
     * c) the name is specified.
     */
    public void testSignupFormValidation()
    {
        enableAnonymousSignup();

        String loginName = String.format("signup-%s", RandomUtils.randomString(4));

        beginAt("/");
        clickLink("signup");

        SignupForm form = new SignupForm(tester);
        form.assertFormPresent();
        
        // no login name
        form.saveFormElements("", "Name", "pass", "pass");
        form.assertFormPresent();

        // no name
        form.saveFormElements(loginName, "", "pass", "pass");
        form.assertFormPresent();

        // mismatching passwords
        form.saveFormElements(loginName, "Name", "pass", "PASS");
        form.assertFormPresent();

        // existing login
    }

    /**
     * Test that the new user has the expected authority, the same as a guest but with the ability to create
     * contact points and configure the dashboard etc.
     *
     * a) can not see the 'Administration' tab.
     */
    public void testAuthorisationOfNewUser()
    {
        enableAnonymousSignup();

        beginAt("/");
        clickLink("signup");

        String loginName = String.format("signup-%s", RandomUtils.randomString(4));

        SignupForm form = new SignupForm(tester);
        form.assertFormPresent();
        form.saveFormElements(loginName, "Some. N. Ame", "pass", "pass");

        form.assertFormNotPresent();

        assertLinkNotPresent("tab.administration");
        assertLinkPresent("tab.projects");
        assertLinkPresent("tab.dashboard");
    }
}
